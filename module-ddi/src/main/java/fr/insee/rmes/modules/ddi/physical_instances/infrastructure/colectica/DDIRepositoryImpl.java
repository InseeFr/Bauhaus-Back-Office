package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static javax.xml.XMLConstants.*;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration.PackageRef;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DDIRepositoryImpl implements DDIRepository {

    static final Logger logger = LoggerFactory.getLogger(
        DDIRepositoryImpl.class
    );

    private static final String BAUHAUS_API = "bauhaus-api";
    private static final Duration MUTUALIZED_CACHE_TTL = Duration.ofMinutes(10);

    private final String defaultLang;

    private final RestClient restClient;
    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaConfiguration colecticaConfiguration;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final DDI4toDDI3ConverterService ddi4ToDdi3Converter;
    private final ColecticaAuthenticator authenticator;

    private volatile CachedCodesList mutualizedCache;

    private record CachedCodesList(List<PartialCodesList> codes, Instant expiresAt) {
        boolean isFresh(Clock clock) {
            return Instant.now(clock).isBefore(expiresAt);
        }
    }

    public DDIRepositoryImpl(
        RestClient restClient,
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        DDI3toDDI4ConverterService ddi3ToDdi4Converter,
        DDI4toDDI3ConverterService ddi4ToDdi3Converter,
        ColecticaConfiguration colecticaConfiguration,
        ColecticaAuthenticator authenticator
    ) {
        this.restClient = restClient;
        this.instanceConfiguration = instanceConfiguration;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
        this.colecticaConfiguration = colecticaConfiguration;
        this.authenticator = authenticator;
        this.defaultLang = colecticaConfiguration.langs().getFirst();
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info(
            "Getting physical instances from Colectica API via HTTP (primary instance)"
        );

        return authenticator.executeWithAuth(token -> {
            // Set up the request with authorization header
            String url = instanceConfiguration.baseApiUrl() + "_query";

            // Create request body with itemTypes from configuration
            QueryRequest requestBody = new QueryRequest(
                List.of(
                    instanceConfiguration.itemTypes().get("PhysicalInstance")
                )
            );

            // Create headers with Bearer token and Content-Type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            ColecticaResponse response = restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(requestBody)
                .retrieve()
                .body(ColecticaResponse.class);

            return response
                .results()
                .stream()
                .map(item -> {
                    String id = item.identifier();
                    String label = extractLabelFromItem(item);
                    SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss"
                    );
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = null;
                    try {
                        date = formatter.parse(item.versionDate());
                    } catch (ParseException | NullPointerException _) {
                        logger.debug("Impossible to parse");
                    }
                    String agency = item.agencyId();
                    return new PartialPhysicalInstance(id, label, date, agency);
                })
                .toList();
        });
    }

    @Override
    public List<PartialGroup> getGroups() {
        logger.info("Getting groups from Colectica API via HTTP");

        return authenticator.executeWithAuth(token -> {
            String url = instanceConfiguration.baseApiUrl() + "_query";
            QueryRequest requestBody = new QueryRequest(
                List.of("4bd6eef6-99df-40e6-9b11-5b8f64e5cb23")
            );
            ColecticaResponse response = restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(requestBody)
                .retrieve()
                .body(ColecticaResponse.class);

            if (
                response == null ||
                response.results() == null ||
                response.results().isEmpty()
            ) {
                return List.<PartialGroup>of();
            }

            // Batch-fetch full XML for all groups to extract seriesIris (UserID elements)
            List<GetDescriptionsRequest.IdentifierRef> identifiers = response
                .results()
                .stream()
                .map(item ->
                    new GetDescriptionsRequest.IdentifierRef(
                        item.agencyId(),
                        item.identifier(),
                        item.version()
                    )
                )
                .toList();

            String getListUrl =
                instanceConfiguration.baseApiUrl() + "item/_getList";
            ColecticaItemResponse[] itemResponses = restClient
                .post()
                .uri(getListUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new GetDescriptionsRequest(identifiers))
                .retrieve()
                .body(ColecticaItemResponse[].class);

            Map<String, List<String>> seriesIrisByGroupId = new HashMap<>();
            if (itemResponses != null) {
                for (ColecticaItemResponse itemResponse : itemResponses) {
                    seriesIrisByGroupId.put(
                        itemResponse.identifier(),
                        extractUserIdsFromGroupXml(itemResponse.item())
                    );
                }
            }

            return response
                .results()
                .stream()
                .map(item -> {
                    String id = item.identifier();
                    String label = extractLabelFromItem(item);
                    SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss"
                    );
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = null;
                    try {
                        date = formatter.parse(item.versionDate());
                    } catch (ParseException | NullPointerException _) {
                        logger.debug(
                            "Impossible to parse {}",
                            item.versionDate()
                        );
                    }
                    String agency = item.agencyId();
                    List<String> seriesIris = seriesIrisByGroupId.getOrDefault(
                        id,
                        List.of()
                    );
                    return new PartialGroup(
                        id,
                        label,
                        date,
                        agency,
                        seriesIris
                    );
                })
                .toList();
        });
    }

    private List<String> extractUserIdsFromGroupXml(String xml) {
        if (xml == null || xml.isBlank()) {
            return List.of();
        }
        try {
            DocumentBuilderFactory factory =
                createSecureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                new InputSource(new StringReader(xml))
            );
            NodeList userIdNodes = doc.getElementsByTagNameNS(
                "ddi:reusable:3_3",
                "UserID"
            );
            List<String> userIds = new ArrayList<>();
            for (int i = 0; i < userIdNodes.getLength(); i++) {
                String text = userIdNodes.item(i).getTextContent();
                if (text != null && !text.isBlank()) {
                    userIds.add(text.trim());
                }
            }
            return userIds;
        } catch (Exception e) {
            logger.warn("Failed to parse group XML to extract UserIDs", e);
            return List.of();
        }
    }

    @Override
    public List<PartialStudyUnit> getStudyUnits() {
        logger.info("Getting study units from Colectica API via HTTP");

        return authenticator.executeWithAuth(token -> {
            String url = instanceConfiguration.baseApiUrl() + "_query";

            QueryRequest requestBody = new QueryRequest(
                List.of("30ea0200-7121-4f01-8d21-a931a182b86d")
            );

            ColecticaResponse response = restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(requestBody)
                .retrieve()
                .body(ColecticaResponse.class);

            return response
                .results()
                .stream()
                .map(item -> {
                    String id = item.identifier();
                    String label = extractLabelFromItem(item);
                    SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss"
                    );
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = null;
                    try {
                        date = formatter.parse(item.versionDate());
                    } catch (ParseException | NullPointerException _) {
                        logger.debug(
                            "Impossible to parse {}",
                            item.versionDate()
                        );
                    }
                    String agency = item.agencyId();
                    return new PartialStudyUnit(id, label, date, agency);
                })
                .toList();
        });
    }

    private String extractLabelFromItem(ColecticaItem item) {
        // Extract value from ItemName or Label (both can have language variants)
        String label = extractLabelFromLanguageMap(item.itemName());
        if (label == null || label.trim().isEmpty()) {
            label = extractLabelFromLanguageMap(item.label());
        }
        if (label == null || label.trim().isEmpty()) {
            label = item.identifier(); // Fallback to ID if no value found
        }
        return label;
    }

    /**
     * Strict label extraction for mutualized code lists: returns the first non-blank value
     * across {@code itemName} then {@code label}, trying default lang, then "en", then any
     * other language. No fallback to identifier — empty Optional if no non-blank value exists.
     */
    private Optional<String> extractStrictLabel(ColecticaItem item) {
        return firstNonBlank(item.itemName())
            .or(() -> firstNonBlank(item.label()));
    }

    private Optional<String> firstNonBlank(Map<String, String> languageMap) {
        if (languageMap == null) return Optional.empty();
        String preferred = languageMap.get(defaultLang);
        if (preferred != null && !preferred.isBlank()) return Optional.of(preferred);
        String english = languageMap.get("en");
        if (english != null && !english.isBlank()) return Optional.of(english);
        return languageMap.values().stream()
            .filter(v -> v != null && !v.isBlank())
            .findFirst();
    }

    private String extractLabelFromLanguageMap(
        Map<String, String> languageMap
    ) {
        if (languageMap == null) {
            return null;
        }

        // Try default lang first, then English, then first available
        String label = languageMap.get(defaultLang);
        if (label == null || label.trim().isEmpty()) {
            label = languageMap.get("en");
        }
        if (label == null || label.trim().isEmpty()) {
            label = languageMap.values().stream().findFirst().orElse(null);
        }
        return label;
    }

    /**
     * Parse FragmentInstance XML and extract each Fragment as a separate Ddi3Item
     * The FragmentInstance contains multiple Fragment elements (PhysicalInstance, DataRelationship, etc.)
     */
    private List<Ddi3Response.Ddi3Item> parseFragmentInstanceToItems(
        String fragmentInstanceXml,
        String agencyId,
        String id
    ) {
        try {
            // Parse the XML with XXE protection
            DocumentBuilderFactory factory =
                createSecureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                new InputSource(new StringReader(fragmentInstanceXml))
            );

            List<Ddi3Response.Ddi3Item> items = new ArrayList<>();

            // Get all Fragment elements
            NodeList fragmentNodes = doc.getElementsByTagNameNS(
                "ddi:instance:3_3",
                "Fragment"
            );

            for (int i = 0; i < fragmentNodes.getLength(); i++) {
                Element fragmentElement = (Element) fragmentNodes.item(i);

                // Determine the item type based on the content of the Fragment
                String itemType = determineItemType(fragmentElement);

                // Skip fragments with unsupported types (e.g., CodeList, Category)
                if (itemType == null) {
                    continue;
                }

                // Convert this Fragment element back to XML string
                String fragmentXml = elementToString(fragmentElement);

                // Create a Ddi3Item for this Fragment
                Ddi3Response.Ddi3Item ddi3Item = new Ddi3Response.Ddi3Item(
                    itemType,
                    agencyId,
                    "1", // version
                    id,
                    fragmentXml,
                    null, // versionDate
                    null, // versionResponsibility
                    false, // isPublished
                    false, // isDeprecated
                    false, // isProvisional
                    instanceConfiguration.itemFormat() // itemFormat
                );

                items.add(ddi3Item);
            }

            return items;
        } catch (Exception e) {
            logger.error("Error parsing FragmentInstance XML", e);
            throw new RuntimeException(
                "Failed to parse FragmentInstance XML",
                e
            );
        }
    }

    /**
     * Determine the item type based on the content of a Fragment element
     * Returns null if the Fragment type is not supported
     */
    private String determineItemType(Element fragmentElement) {
        // Check for PhysicalInstance
        if (
            fragmentElement
                .getElementsByTagNameNS(
                    "ddi:physicalinstance:3_3",
                    "PhysicalInstance"
                )
                .getLength() >
            0
        ) {
            return instanceConfiguration.itemTypes().get("PhysicalInstance");
        }
        // Check for DataRelationship
        if (
            fragmentElement
                .getElementsByTagNameNS(
                    "ddi:logicalproduct:3_3",
                    "DataRelationship"
                )
                .getLength() >
            0
        ) {
            return instanceConfiguration.itemTypes().get("DataRelationship");
        }
        // Check for Variable
        if (
            fragmentElement
                .getElementsByTagNameNS("ddi:logicalproduct:3_3", "Variable")
                .getLength() >
            0
        ) {
            return instanceConfiguration.itemTypes().get("Variable");
        }
        // Check for CodeList
        if (
            fragmentElement
                .getElementsByTagNameNS("ddi:logicalproduct:3_3", "CodeList")
                .getLength() >
            0
        ) {
            return instanceConfiguration.itemTypes().get("CodeList");
        }
        // Check for Category
        if (
            fragmentElement
                .getElementsByTagNameNS("ddi:logicalproduct:3_3", "Category")
                .getLength() >
            0
        ) {
            return instanceConfiguration.itemTypes().get("Category");
        }
        // Return null for unsupported types
        return null;
    }

    /**
     * Creates a secure DocumentBuilderFactory with XXE protection
     */
    private DocumentBuilderFactory createSecureDocumentBuilderFactory()
        throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(true);
        return factory;
    }

    /**
     * Creates a secure TransformerFactory with XXE protection
     */
    private TransformerFactory createSecureTransformerFactory()
        throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");
        return factory;
    }

    /**
     * Convert a DOM Element to XML String
     */
    private String elementToString(Element element) throws Exception {
        TransformerFactory transformerFactory =
            createSecureTransformerFactory();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }

    @Override
    public Ddi4Response getPhysicalInstance(String agencyId, String id) {
        return authenticator.executeWithAuth(token -> {
            try {
                String setUrl =
                    instanceConfiguration.baseApiUrl() +
                    "set/" +
                    agencyId +
                    "/" +
                    id;
                ColecticaSetItem[] setItems = restClient
                    .get()
                    .uri(setUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(ColecticaSetItem[].class);

                if (setItems == null || setItems.length == 0) {
                    return null;
                }

                List<GetDescriptionsRequest.IdentifierRef> identifiers =
                    Arrays.stream(setItems)
                        .map(item ->
                            new GetDescriptionsRequest.IdentifierRef(
                                item.agencyId(),
                                item.identifier(),
                                item.version()
                            )
                        )
                        .toList();

                String getListUrl =
                    instanceConfiguration.baseApiUrl() + "item/_getList";
                ColecticaItemResponse[] itemResponses = restClient
                    .post()
                    .uri(getListUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(new GetDescriptionsRequest(identifiers))
                    .retrieve()
                    .body(ColecticaItemResponse[].class);

                if (itemResponses == null || itemResponses.length == 0) {
                    return null;
                }

                List<Ddi3Response.Ddi3Item> ddi3Items = Arrays.stream(
                    itemResponses
                )
                    .map(item ->
                        new Ddi3Response.Ddi3Item(
                            item.itemType(),
                            item.agencyId(),
                            String.valueOf(item.version()),
                            item.identifier(),
                            item.item(),
                            item.versionDate(),
                            item.versionResponsibility(),
                            item.isPublished(),
                            item.isDeprecated(),
                            item.isProvisional(),
                            item.itemFormat()
                        )
                    )
                    .toList();

                Ddi3Response ddi3Response = new Ddi3Response(null, ddi3Items);

                logger.info("Converting DDI3 to DDI4 using converter service");
                var response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                    ddi3Response,
                    "ddi:4.0"
                );

                logger.info(
                    "Successfully converted Physical Instance to DDI4 format"
                );
                return filterMutualizedCodeLists(response);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process DDI response", e);
            }
        });
    }

    private Ddi4Response filterMutualizedCodeLists(Ddi4Response response) {
        if (response == null || response.codeList() == null || response.codeList().isEmpty()) {
            return response;
        }
        Set<String> mutualizedKeys = getMutualizedCodesLists().stream()
                .map(p -> p.agency() + "/" + p.id())
                .collect(Collectors.toSet());
        if (mutualizedKeys.isEmpty()) {
            return response;
        }
        List<Ddi4CodeList> kept = response.codeList().stream()
                .filter(cl -> !mutualizedKeys.contains(cl.agency() + "/" + cl.id()))
                .toList();
        if (kept.size() == response.codeList().size()) {
            return response;
        }
        return new Ddi4Response(
                response.schema(),
                response.topLevelReference(),
                response.physicalInstance(),
                response.dataRelationship(),
                response.variable(),
                kept.isEmpty() ? null : kept,
                response.category()
        );
    }

    @Override
    public Ddi4GroupResponse getGroup(String agencyId, String id) {
        logger.info(
            "Fetching DDI4 Group from Colectica API for agencyId: {}, id: {}",
            agencyId,
            id
        );

        return authenticator.executeWithAuth(token -> {
            try {
                // Fetch the full DDI set (Group + StudyUnits) using the ddiset endpoint
                String ddisetUrl =
                    instanceConfiguration.baseApiUrl() +
                    "ddiset/" +
                    agencyId +
                    "/" +
                    id;

                logger.info(
                    "Fetching full DDI set for Group from: {}",
                    ddisetUrl
                );

                // Read raw bytes and decode as UTF-8 explicitly. body(String.class) lets
                // Spring's StringHttpMessageConverter pick the charset from the response's
                // Content-Type, which Colectica omits — Spring then falls back to ISO-8859-1
                // and produces mojibake on accented characters.
                byte[] ddisetBytes = restClient
                    .get()
                    .uri(ddisetUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(byte[].class);

                if (ddisetBytes == null || ddisetBytes.length == 0) {
                    logger.error(
                        "Received empty response from Colectica API for ddiset URL: {}",
                        ddisetUrl
                    );
                    return null;
                }

                String ddisetXml = new String(ddisetBytes, StandardCharsets.UTF_8);

                logger.info(
                    "Received response from ddiset endpoint for Group. Length: {}",
                    ddisetXml.length()
                );

                // Clean the XML - remove leading invisible/control characters
                int startIndex = 0;
                while (
                    startIndex < ddisetXml.length() &&
                    ddisetXml.charAt(startIndex) != '<'
                ) {
                    char c = ddisetXml.charAt(startIndex);
                    if (
                        c == '\uFEFF' ||
                        Character.isWhitespace(c) ||
                        Character.isISOControl(c) ||
                        !Character.isDefined(c)
                    ) {
                        startIndex++;
                    } else {
                        logger.warn(
                            "Unexpected character at position {}: {} (code: {})",
                            startIndex,
                            c,
                            (int) c
                        );
                        startIndex++;
                    }
                }

                if (startIndex > 0) {
                    logger.info(
                        "Removed {} leading characters from XML",
                        startIndex
                    );
                    ddisetXml = ddisetXml.substring(startIndex);
                }

                ddisetXml = ddisetXml.trim();

                // Parse the XML directly to build Ddi4GroupResponse
                return parseGroupXmlToDdi4Response(ddisetXml);
            } catch (Exception e) {
                logger.error(
                    "Error processing Colectica API response for Group agencyId: {}, id: {}",
                    agencyId,
                    id,
                    e
                );
                throw new RuntimeException(
                    "Failed to process DDI Group response",
                    e
                );
            }
        });
    }

    /**
     * Parse DDI3 XML directly to build Ddi4GroupResponse
     */
    private Ddi4GroupResponse parseGroupXmlToDdi4Response(String xml)
        throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        List<Ddi4Group> groups = new ArrayList<>();
        List<Ddi4StudyUnit> studyUnits = new ArrayList<>();
        List<Reference> topLevelReferences = new ArrayList<>();

        // Parse Group elements
        NodeList groupNodes = doc.getElementsByTagNameNS(
            "ddi:group:3_3",
            "Group"
        );
        logger.info("Found {} Group elements", groupNodes.getLength());

        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element groupElement = (Element) groupNodes.item(i);
            Ddi4Group group = parseGroupElement(groupElement);
            groups.add(group);

            // Add top level reference for the group
            topLevelReferences.add(
                Reference.of(
                    group.agency(),
                    group.id(),
                    group.version(),
                    "Group"
                )
            );
        }

        // Parse StudyUnit elements
        NodeList studyUnitNodes = doc.getElementsByTagNameNS(
            "ddi:studyunit:3_3",
            "StudyUnit"
        );
        logger.info("Found {} StudyUnit elements", studyUnitNodes.getLength());

        for (int i = 0; i < studyUnitNodes.getLength(); i++) {
            Element studyUnitElement = (Element) studyUnitNodes.item(i);
            Ddi4StudyUnit studyUnit = parseStudyUnitElement(studyUnitElement);
            studyUnits.add(studyUnit);
        }

        return new Ddi4GroupResponse(
            "ddi:4.0",
            topLevelReferences,
            groups,
            studyUnits
        );
    }

    /**
     * Parse a Group XML element to Ddi4Group
     */
    private Ddi4Group parseGroupElement(Element groupElement) {
        String versionDate = groupElement.getAttribute("versionDate");

        String urn = getElementTextContent(
            groupElement,
            "ddi:reusable:3_3",
            "URN"
        );
        String agency = getElementTextContent(
            groupElement,
            "ddi:reusable:3_3",
            "Agency"
        );
        String id = getElementTextContent(
            groupElement,
            "ddi:reusable:3_3",
            "ID"
        );
        String version = getElementTextContent(
            groupElement,
            "ddi:reusable:3_3",
            "Version"
        );
        String versionResponsibility = getElementTextContent(
            groupElement,
            "ddi:reusable:3_3",
            "VersionResponsibility"
        );

        // Parse Citation
        Citation citation = parseCitation(groupElement);

        // Parse StudyUnitReferences
        List<Reference> studyUnitReferences = parseStudyUnitReferences(
            groupElement
        );

        // Parse UserID elements (seriesIris) and TypeOfGroup
        List<String> seriesIris = getAllElementTextContents(
            groupElement,
            "ddi:reusable:3_3",
            "UserID"
        );
        String typeOfGroup = getElementTextContent(
            groupElement,
            "ddi:group:3_3",
            "TypeOfGroup"
        );

        return new Ddi4Group(
            Ddi4Group.TYPE,
            versionDate.isEmpty() ? null : CogsDate.ofDateTime(versionDate),
            urn,
            agency,
            id,
            version,
            versionResponsibility,
            citation,
            studyUnitReferences,
            seriesIris.isEmpty() ? null : seriesIris,
            (typeOfGroup == null || typeOfGroup.isEmpty()) ? null : typeOfGroup
        );
    }

    /**
     * Parse a StudyUnit XML element to Ddi4StudyUnit
     */
    private Ddi4StudyUnit parseStudyUnitElement(Element studyUnitElement) {
        String versionDate = studyUnitElement.getAttribute("versionDate");

        String urn = getElementTextContent(
            studyUnitElement,
            "ddi:reusable:3_3",
            "URN"
        );
        String agency = getElementTextContent(
            studyUnitElement,
            "ddi:reusable:3_3",
            "Agency"
        );
        String id = getElementTextContent(
            studyUnitElement,
            "ddi:reusable:3_3",
            "ID"
        );
        String version = getElementTextContent(
            studyUnitElement,
            "ddi:reusable:3_3",
            "Version"
        );

        // Parse Citation
        Citation citation = parseCitation(studyUnitElement);

        // Parse UserID (operationIri)
        String operationIri = getElementTextContent(
            studyUnitElement,
            "ddi:reusable:3_3",
            "UserID"
        );

        return new Ddi4StudyUnit(
            Ddi4StudyUnit.TYPE,
            (versionDate == null || versionDate.isEmpty()) ? null : CogsDate.ofDateTime(versionDate),
            urn,
            agency,
            id,
            version,
            citation,
            (operationIri == null || operationIri.isEmpty())
                ? null
                : operationIri,
            null
        );
    }

    /**
     * Parse Citation from an element
     */
    private Citation parseCitation(Element parentElement) {
        NodeList citationNodes = parentElement.getElementsByTagNameNS(
            "ddi:reusable:3_3",
            "Citation"
        );
        if (citationNodes.getLength() == 0) {
            return null;
        }

        Element citationElement = (Element) citationNodes.item(0);
        NodeList titleNodes = citationElement.getElementsByTagNameNS(
            "ddi:reusable:3_3",
            "Title"
        );
        if (titleNodes.getLength() == 0) {
            return null;
        }

        Element titleElement = (Element) titleNodes.item(0);
        NodeList stringNodes = titleElement.getElementsByTagNameNS(
            "ddi:reusable:3_3",
            "String"
        );
        if (stringNodes.getLength() == 0) {
            return null;
        }

        Element stringElement = (Element) stringNodes.item(0);
        String text = stringElement.getTextContent();
        String lang = stringElement.getAttributeNS(
            "http://www.w3.org/XML/1998/namespace",
            "lang"
        );

        return new Citation(
            LangStrings.of(lang.isEmpty() ? defaultLang : lang, text)
        );
    }

    /**
     * Parse StudyUnitReferences from a Group element
     */
    private List<Reference> parseStudyUnitReferences(
        Element groupElement
    ) {
        List<Reference> references = new ArrayList<>();

        NodeList refNodes = groupElement.getElementsByTagNameNS(
            "ddi:reusable:3_3",
            "StudyUnitReference"
        );
        for (int i = 0; i < refNodes.getLength(); i++) {
            Element refElement = (Element) refNodes.item(i);

            String agency = getElementTextContent(
                refElement,
                "ddi:reusable:3_3",
                "Agency"
            );
            String id = getElementTextContent(
                refElement,
                "ddi:reusable:3_3",
                "ID"
            );
            String version = getElementTextContent(
                refElement,
                "ddi:reusable:3_3",
                "Version"
            );
            String typeOfObject = getElementTextContent(
                refElement,
                "ddi:reusable:3_3",
                "TypeOfObject"
            );

            references.add(Reference.of(agency, id, version, typeOfObject));
        }

        return references;
    }

    /**
     * Get text content of the first child element with given namespace and local name.
     */
    private String getElementTextContent(
        Element parent,
        String namespaceUri,
        String localName
    ) {
        NodeList nodes = parent.getElementsByTagNameNS(namespaceUri, localName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    /**
     * Get text content of all child elements with given namespace and local name.
     */
    private List<String> getAllElementTextContents(
        Element parent,
        String namespaceUri,
        String localName
    ) {
        NodeList nodes = parent.getElementsByTagNameNS(namespaceUri, localName);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            String text = nodes.item(i).getTextContent();
            if (text != null && !text.isBlank()) {
                values.add(text.trim());
            }
        }
        return values;
    }

    @Override
    public void updatePhysicalInstance(
        String agencyId,
        String id,
        UpdatePhysicalInstanceRequest request
    ) {
        // First, fetch the current instance to get all necessary information including variables
        Ddi4Response currentInstance = getPhysicalInstance(agencyId, id);

        if (
            currentInstance == null ||
            currentInstance.physicalInstance() == null ||
            currentInstance.physicalInstance().isEmpty()
        ) {
            throw new RuntimeException(
                "Physical instance not found: " + agencyId + "/" + id
            );
        }

        var currentPI = currentInstance.physicalInstance().getFirst();
        var currentDR =
            currentInstance.dataRelationship() != null &&
            !currentInstance.dataRelationship().isEmpty()
                ? currentInstance.dataRelationship().getFirst()
                : null;

        // Get current timestamp in ISO format
        String versionDate = ZonedDateTime.now().format(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        );

        // Build updated PhysicalInstance with new label if provided
        LangString currentTitle = currentPI.citation().title().get(0);
        String newPhysicalInstanceLabel =
            request.physicalInstanceLabel() != null
                ? request.physicalInstanceLabel()
                : currentTitle.value();

        var updatedPI = new Ddi4PhysicalInstance(
            Ddi4PhysicalInstance.TYPE,
            CogsDate.ofDateTime(versionDate),
            currentPI.urn(),
            currentPI.agency(),
            currentPI.id(),
            currentPI.version(),
            currentPI.basedOnObject(),
            new Citation(
                LangStrings.of(
                    currentTitle.language(),
                    newPhysicalInstanceLabel
                )
            ),
            currentPI.dataRelationshipReference()
        );

        // Build updated DataRelationship with new label if provided, preserving LogicalRecord with variables
        Ddi4DataRelationship updatedDR = null;
        if (currentDR != null) {
            // Build updated DataRelationship Label
            List<LangString> drLabel = createLabelWithFallback(
                currentDR.label(),
                request.dataRelationshipLabel()
            );

            // Build updated LogicalRecord with new label if provided
            LogicalRecord updatedLR = currentDR.logicalRecord();
            if (updatedLR != null && request.logicalRecordLabel() != null) {
                updatedLR = new LogicalRecord(
                    LogicalRecord.TYPE,
                    updatedLR.urn(),
                    updatedLR.agency(),
                    updatedLR.id(),
                    updatedLR.version(),
                    createLabelWithFallback(
                        updatedLR.label(),
                        request.logicalRecordLabel()
                    ),
                    updatedLR.variablesInRecord()
                );
            }

            updatedDR = new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime(versionDate),
                currentDR.urn(),
                currentDR.agency(),
                currentDR.id(),
                currentDR.version(),
                currentDR.basedOnObject(),
                drLabel,
                updatedLR // Updated LogicalRecord with new label
            );
        }

        // Build updated Ddi4Response preserving all variables, codeLists and categories
        Ddi4Response updatedResponse = new Ddi4Response(
            currentInstance.schema(),
            currentInstance.topLevelReference(),
            List.of(updatedPI),
            updatedDR != null
                ? List.of(updatedDR)
                : currentInstance.dataRelationship(),
            currentInstance.variable(), // Preserve all variables
            currentInstance.codeList(), // Preserve all codeLists
            currentInstance.category() // Preserve all categories
        );

        // Use updateFullPhysicalInstance to save everything including variables
        updateFullPhysicalInstance(agencyId, id, updatedResponse);
    }

    @Override
    public void updateFullPhysicalInstance(
        String agencyId,
        String id,
        Ddi4Response ddi4Response
    ) {
        logger.info(
            "Updating full physical instance {}/{} with all DDI objects in Colectica",
            agencyId,
            id
        );

        authenticator.executeWithAuth(token -> {
            // Convert DDI4 to DDI3
            Ddi3Response ddi3Response = ddi4ToDdi3Converter.convertDdi4ToDdi3(
                ddi4Response
            );

            if (
                ddi3Response == null ||
                ddi3Response.items() == null ||
                ddi3Response.items().isEmpty()
            ) {
                throw new RuntimeException("No items to save in DDI4 response");
            }

            // Convert each Ddi3Item to ColecticaItemResponse
            List<ColecticaItemResponse> colecticaItems = ddi3Response
                .items()
                .stream()
                .map(ddi3Item ->
                    new ColecticaItemResponse(
                        ddi3Item.itemType(),
                        ddi3Item.agencyId(),
                        Integer.parseInt(ddi3Item.version()),
                        ddi3Item.identifier(),
                        ddi3Item.item(),
                        ddi3Item.versionDate(),
                        ddi3Item.versionResponsibility(),
                        ddi3Item.isPublished(),
                        ddi3Item.isDeprecated(),
                        ddi3Item.isProvisional(),
                        ddi3Item.itemFormat()
                    )
                )
                .toList();

            // Create request with all items
            ColecticaCreateItemRequest updateRequest =
                new ColecticaCreateItemRequest(colecticaItems);

            // Send to Colectica
            String url = instanceConfiguration.baseApiUrl() + "item";

            logger.info(
                "Sending full update request to Colectica with {} items: {}",
                colecticaItems.size(),
                url
            );

            restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(updateRequest)
                .retrieve()
                .body(String.class);

            logger.info(
                "Successfully updated full physical instance with id: {} ({} items saved)",
                id,
                colecticaItems.size()
            );

            return null;
        });
    }

    @Override
    public Ddi4Response createPhysicalInstance(
        CreatePhysicalInstanceRequest request
    ) {
        return authenticator.executeWithAuth(token -> {
            // Generate UUIDs for physical instance and data relationship
            String physicalInstanceId = UUID.randomUUID().toString();
            String dataRelationshipId = UUID.randomUUID().toString();
            String logicalRecordId = UUID.randomUUID().toString();
            String agencyId = instanceConfiguration.defaultAgencyId();
            int version = 1;

            // Get current timestamp in ISO format
            String versionDate = ZonedDateTime.now().format(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            );

            // Build DDI3 XML fragments
            String physicalInstanceXml = buildPhysicalInstanceXml(
                agencyId,
                physicalInstanceId,
                version,
                request.physicalInstanceLabel(),
                dataRelationshipId,
                versionDate
            );

            String dataRelationshipXml = buildDataRelationshipXml(
                agencyId,
                dataRelationshipId,
                version,
                request.dataRelationshipLabel(),
                logicalRecordId,
                request.logicalRecordLabel() != null
                    ? request.logicalRecordLabel()
                    : request.physicalInstanceLabel(),
                versionDate
            );

            // Create Colectica items
            ColecticaItemResponse physicalInstanceItem =
                new ColecticaItemResponse(
                    instanceConfiguration.itemTypes().get("PhysicalInstance"),
                    agencyId,
                    version,
                    physicalInstanceId,
                    physicalInstanceXml,
                    versionDate,
                    BAUHAUS_API,
                    false, // isPublished
                    false, // isDeprecated
                    false, // isProvisional
                    instanceConfiguration.itemFormat() // DDI format UUID
                );

            ColecticaItemResponse dataRelationshipItem =
                new ColecticaItemResponse(
                    instanceConfiguration.itemTypes().get("DataRelationship"),
                    agencyId,
                    version,
                    dataRelationshipId,
                    dataRelationshipXml,
                    versionDate,
                    BAUHAUS_API,
                    false, // isPublished
                    false, // isDeprecated
                    false, // isProvisional
                    instanceConfiguration.itemFormat() // DDI format UUID
                );

            List<ColecticaItemResponse> itemsToCreate = new ArrayList<>(
                List.of(physicalInstanceItem, dataRelationshipItem)
            );

            if (
                request.studyUnitId() != null &&
                request.studyUnitAgency() != null
            ) {
                ColecticaItemResponse updatedStudyUnit =
                    addPhysicalInstanceReferenceToStudyUnit(
                        request.studyUnitAgency(),
                        request.studyUnitId(),
                        agencyId,
                        physicalInstanceId
                    );
                itemsToCreate.add(updatedStudyUnit);
            }

            ColecticaCreateItemRequest createRequest =
                new ColecticaCreateItemRequest(itemsToCreate);

            // Send to Colectica
            String url = instanceConfiguration.baseApiUrl() + "item";

            restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(createRequest)
                .retrieve()
                .body(String.class);

            // Return the created instance
            return getPhysicalInstance(agencyId, physicalInstanceId);
        });
    }

    /**
     * Build DDI3 XML fragment for PhysicalInstance
     */
    private String generateDataRelationshipReference(
        String agencyId,
        String dataRelationshipId,
        int version
    ) {
        return String.format(
            """
            <r:DataRelationshipReference>
              <r:Agency>%s</r:Agency>
              <r:ID>%s</r:ID>
              <r:Version>%d</r:Version>
              <r:TypeOfObject>DataRelationship</r:TypeOfObject>
            </r:DataRelationshipReference>""",
            escapeXml(agencyId),
            escapeXml(dataRelationshipId),
            version
        );
    }

    private String buildPhysicalInstanceXml(
        String agencyId,
        String id,
        int version,
        String label,
        String dataRelationshipId,
        String versionDate
    ) {
        return String.format(
            """
            <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
              <PhysicalInstance isUniversallyUnique="true" versionDate="%s" xmlns="ddi:physicalinstance:3_3">
                <r:URN>urn:ddi:%s:%s:%d</r:URN>
                <r:Agency>%s</r:Agency>
                <r:ID>%s</r:ID>
                <r:Version>%d</r:Version>
                <r:Citation>
                  <r:Title>
                    <r:String xml:lang="%s">%s</r:String>
                  </r:Title>
                </r:Citation>
                %s
              </PhysicalInstance>
            </Fragment>""",
            escapeXml(versionDate),
            escapeXml(agencyId),
            escapeXml(id),
            version,
            escapeXml(agencyId),
            escapeXml(id),
            version,
            defaultLang,
            escapeXml(label),
            generateDataRelationshipReference(
                agencyId,
                dataRelationshipId,
                version
            )
        );
    }

    /**
     * Build DDI3 XML fragment for DataRelationship
     */
    private String buildDataRelationshipXml(
        String agencyId,
        String dataRelationshipId,
        int version,
        String dataRelationshipLabel,
        String logicalRecordId,
        String logicalRecordLabel,
        String versionDate
    ) {
        return String.format(
            """
            <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
              <DataRelationship isUniversallyUnique="true" versionDate="%s" xmlns="ddi:logicalproduct:3_3">
                <r:URN>urn:ddi:%s:%s:%d</r:URN>
                <r:Agency>%s</r:Agency>
                <r:ID>%s</r:ID>
                <r:Version>%d</r:Version>
                <r:Label>
                  <r:Content xml:lang="%s">%s</r:Content>
                </r:Label>
                <LogicalRecord isUniversallyUnique="true">
                  <r:URN>urn:ddi:%s:%s:%d</r:URN>
                  <r:Agency>%s</r:Agency>
                  <r:ID>%s</r:ID>
                  <r:Version>%d</r:Version>
                  <r:Label>
                    <r:Content xml:lang="%s">%s</r:Content>
                  </r:Label>
                </LogicalRecord>
              </DataRelationship>
            </Fragment>""",
            escapeXml(versionDate),
            escapeXml(agencyId),
            escapeXml(dataRelationshipId),
            version,
            escapeXml(agencyId),
            escapeXml(dataRelationshipId),
            version,
            defaultLang,
            escapeXml(dataRelationshipLabel),
            escapeXml(agencyId),
            escapeXml(logicalRecordId),
            version,
            escapeXml(agencyId),
            escapeXml(logicalRecordId),
            version,
            defaultLang,
            escapeXml(logicalRecordLabel)
        );
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    /**
     * Returns the full DDI4 representation of one mutualized code list (codes + categories)
     * via the {@code set/} + {@code _getList} + DDI3→DDI4 conversion pipeline.
     */
    @Override
    public Ddi4Response getMutualizedCodesList(String agencyId, String id) {
        logger.info("Fetching mutualized codes list {}/{}", agencyId, id);

        return authenticator.executeWithAuth(token -> {
            try {
                String setUrl =
                    instanceConfiguration.baseApiUrl() +
                    "set/" +
                    agencyId +
                    "/" +
                    id;
                ColecticaSetItem[] setItems = restClient
                    .get()
                    .uri(setUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(ColecticaSetItem[].class);

                if (setItems == null || setItems.length == 0) {
                    return null;
                }

                List<GetDescriptionsRequest.IdentifierRef> identifiers =
                    Arrays.stream(setItems)
                        .map(item ->
                            new GetDescriptionsRequest.IdentifierRef(
                                item.agencyId(),
                                item.identifier(),
                                item.version()
                            )
                        )
                        .toList();

                String getListUrl =
                    instanceConfiguration.baseApiUrl() + "item/_getList";
                ColecticaItemResponse[] itemResponses = restClient
                    .post()
                    .uri(getListUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(new GetDescriptionsRequest(identifiers))
                    .retrieve()
                    .body(ColecticaItemResponse[].class);

                if (itemResponses == null || itemResponses.length == 0) {
                    return null;
                }

                List<Ddi3Response.Ddi3Item> ddi3Items = Arrays.stream(
                    itemResponses
                )
                    .map(item ->
                        new Ddi3Response.Ddi3Item(
                            item.itemType(),
                            item.agencyId(),
                            String.valueOf(item.version()),
                            item.identifier(),
                            item.item(),
                            item.versionDate(),
                            item.versionResponsibility(),
                            item.isPublished(),
                            item.isDeprecated(),
                            item.isProvisional(),
                            item.itemFormat()
                        )
                    )
                    .toList();

                Ddi3Response ddi3Response = new Ddi3Response(null, ddi3Items);
                return ddi3ToDdi4Converter.convertDdi3ToDdi4(
                    ddi3Response,
                    "ddi:4.0"
                );
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to fetch mutualized codes list",
                    e
                );
            }
        });
    }

    /**
     * Returns metadata for every CodeList that is reachable, via its ancestors, from the configured
     * mutualized codes package.
     *
     * <p>Flow:
     * <ol>
     *   <li>{@code POST _query itemTypes=[CodeList]} → every CodeList in the repository.</li>
     *   <li>For each CodeList, walk up the parent chain via
     *       {@code POST _query/relationship/byobject/descriptions} until we reach the configured
     *       package or exhaust the chain. Results are memoized across CodeLists so each ancestor is
     *       queried at most once.</li>
     * </ol>
     *
     * <p>Items are kept only when they carry a non-blank label (no fallback to identifier).
     * Duplicates are removed by {@code agencyId/identifier}.
     *
     * <p>The result is cached in-process for {@link #MUTUALIZED_CACHE_TTL} to avoid hitting
     * Colectica on every request. Concurrent callers see a single recompute.
     */
    @Override
    public List<PartialCodesList> getMutualizedCodesLists() {
        CachedCodesList snapshot = mutualizedCache;
        if (snapshot != null && snapshot.isFresh(Clock.systemUTC())) {
            return snapshot.codes();
        }
        synchronized (this) {
            snapshot = mutualizedCache;
            if (snapshot != null && snapshot.isFresh(Clock.systemUTC())) {
                return snapshot.codes();
            }
            List<PartialCodesList> fresh = computeMutualizedCodesLists();
            mutualizedCache = new CachedCodesList(fresh, Instant.now().plus(MUTUALIZED_CACHE_TTL));
            return fresh;
        }
    }

    private List<PartialCodesList> computeMutualizedCodesLists() {
        PackageRef rootPackage = colecticaConfiguration.mutualizedCodesPackage();
        if (rootPackage == null) {
            return List.of();
        }
        String packageKey = rootPackage.agencyId() + "/" + rootPackage.identifier();
        logger.info("Fetching CodeLists under package {}", packageKey);

        return authenticator.executeWithAuth(token -> {
            String codeListType = instanceConfiguration.itemTypes().get("CodeList");
            String queryUrl = instanceConfiguration.baseApiUrl() + "_query";

            long t0 = System.currentTimeMillis();
            ColecticaResponse response = restClient.post()
                .uri(queryUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new QueryRequest(List.of(codeListType)))
                .retrieve()
                .body(ColecticaResponse.class);
            long queryMs = System.currentTimeMillis() - t0;
            int total = response == null || response.results() == null ? 0 : response.results().size();
            logger.info("_query CodeList returned {} items in {} ms", total, queryMs);

            if (response == null || response.results() == null) {
                return List.of();
            }

            Map<String, Boolean> descendantCache = new HashMap<>();
            descendantCache.put(packageKey, true);

            Map<String, PartialCodesList> collected = new LinkedHashMap<>();
            long t1 = System.currentTimeMillis();
            int parentLookups = 0;
            for (ColecticaItem item : response.results()) {
                if (item == null) continue;
                Optional<String> label = extractStrictLabel(item);
                if (label.isEmpty()) continue;
                if (!isDescendantOfPackage(item.agencyId(), item.identifier(), packageKey, descendantCache, token)) {
                    continue;
                }
                String key = item.agencyId() + "/" + item.identifier();
                collected.putIfAbsent(key, new PartialCodesList(
                    item.identifier(), label.get(), parseColecticaDate(item.versionDate()), item.agencyId()
                ));
                parentLookups++;
            }
            logger.info("Ancestor checks on {} CodeLists in {} ms; {} kept; cache size {}",
                parentLookups, System.currentTimeMillis() - t1, collected.size(), descendantCache.size());

            return List.copyOf(collected.values());
        });
    }

    private boolean isDescendantOfPackage(
        String agencyId, String identifier, String packageKey,
        Map<String, Boolean> cache, String token
    ) {
        String key = agencyId + "/" + identifier;
        if (cache.containsKey(key)) return cache.get(key);
        // tentative false to break cycles
        cache.put(key, false);

        String url = instanceConfiguration.baseApiUrl() + "_query/relationship/byobject/descriptions";
        ColecticaParentRef[] parents;
        try {
            parents = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new RelationshipBySubjectRequest(
                    List.of(),
                    new RelationshipBySubjectRequest.TargetItemRef(agencyId, identifier)
                ))
                .retrieve()
                .body(ColecticaParentRef[].class);
        } catch (RuntimeException e) {
            logger.warn("byobject lookup failed for {}/{}: {}", agencyId, identifier, e.getMessage());
            return false;
        }
        if (parents == null) return false;
        for (ColecticaParentRef parent : parents) {
            if (isDescendantOfPackage(parent.agencyId(), parent.identifier(), packageKey, cache, token)) {
                cache.put(key, true);
                return true;
            }
        }
        return false;
    }

    private static Date parseColecticaDate(String raw) {
        if (raw == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return formatter.parse(raw);
        } catch (ParseException _) {
            return null;
        }
    }

    /**
     * Creates a Label with the given text, using the language from the existing label if available,
     * or falling back to the default language (first entry of {@code ColecticaConfiguration.langs()}).
     *
     * @param existingLabel the existing label to extract language from (can be null)
     * @param newText the text for the new label
     * @return a new localized label with the appropriate language, or null if newText is null
     */
    private List<LangString> createLabelWithFallback(List<LangString> existingLabel, String newText) {
        if (newText == null) {
            return existingLabel;
        }
        String lang =
            existingLabel != null && !existingLabel.isEmpty()
                ? existingLabel.get(0).language()
                : defaultLang;
        return LangStrings.of(lang, newText);
    }

    @Override
    public String getItemXml(String agency, String id, String version) {
        ColecticaItemResponse response = fetchColecticaItem(
            agency,
            id,
            version
        );
        return response != null ? response.item() : null;
    }

    @Override
    public String getItemXml(String agency, String id) {
        ColecticaItemResponse response = fetchColecticaItem(agency, id, null);
        return response != null ? response.item() : null;
    }

    private ColecticaItemResponse fetchColecticaItem(
        String agency,
        String id,
        String version
    ) {
        return authenticator.executeWithAuth(token -> {
            String encodedAgency = URLEncoder.encode(
                agency,
                StandardCharsets.UTF_8
            );
            String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);

            String url =
                instanceConfiguration.baseApiUrl() +
                "item/" +
                encodedAgency +
                "/" +
                encodedId;
            if (version != null && !version.isBlank()) {
                url += "/" + URLEncoder.encode(version, StandardCharsets.UTF_8);
            }

            return restClient
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(ColecticaItemResponse.class);
        });
    }

    private ColecticaItemResponse addPhysicalInstanceReferenceToStudyUnit(
        String studyUnitAgency,
        String studyUnitId,
        String physicalInstanceAgency,
        String physicalInstanceId
    ) {
        ColecticaItemResponse studyUnitItem = fetchColecticaItem(
            studyUnitAgency,
            studyUnitId,
            null
        );
        if (studyUnitItem == null) {
            throw new RuntimeException(
                "StudyUnit not found: agency=" +
                    studyUnitAgency +
                    " id=" +
                    studyUnitId
            );
        }
        try {
            DocumentBuilderFactory factory =
                createSecureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                new InputSource(new StringReader(studyUnitItem.item()))
            );

            NodeList studyUnitNodes = doc.getElementsByTagNameNS(
                "ddi:studyunit:3_3",
                "StudyUnit"
            );
            if (studyUnitNodes.getLength() == 0) {
                throw new RuntimeException(
                    "No StudyUnit element found in XML for id=" + studyUnitId
                );
            }
            Element studyUnitElement = (Element) studyUnitNodes.item(0);

            Element piRef = doc.createElementNS(
                "ddi:reusable:3_3",
                "r:PhysicalInstanceReference"
            );

            Element agencyEl = doc.createElementNS(
                "ddi:reusable:3_3",
                "r:Agency"
            );
            agencyEl.setTextContent(physicalInstanceAgency);
            piRef.appendChild(agencyEl);

            Element idEl = doc.createElementNS("ddi:reusable:3_3", "r:ID");
            idEl.setTextContent(physicalInstanceId);
            piRef.appendChild(idEl);

            Element versionEl = doc.createElementNS(
                "ddi:reusable:3_3",
                "r:Version"
            );
            versionEl.setTextContent("1");
            piRef.appendChild(versionEl);

            Element typeEl = doc.createElementNS(
                "ddi:reusable:3_3",
                "r:TypeOfObject"
            );
            typeEl.setTextContent("PhysicalInstance");
            piRef.appendChild(typeEl);

            studyUnitElement.appendChild(piRef);

            TransformerFactory transformerFactory =
                createSecureTransformerFactory();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(
                OutputKeys.OMIT_XML_DECLARATION,
                "yes"
            );
            StringWriter writer = new StringWriter();
            transformer.transform(
                new DOMSource(doc.getDocumentElement()),
                new StreamResult(writer)
            );
            String updatedXml = writer.toString();

            return new ColecticaItemResponse(
                instanceConfiguration.itemTypes().get("StudyUnit"),
                studyUnitAgency,
                studyUnitItem.version(),
                studyUnitId,
                updatedXml,
                studyUnitItem.versionDate(),
                studyUnitItem.versionResponsibility(),
                studyUnitItem.isPublished(),
                studyUnitItem.isDeprecated(),
                studyUnitItem.isProvisional(),
                instanceConfiguration.itemFormat()
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to add PhysicalInstanceReference to StudyUnit id=" +
                    studyUnitId,
                e
            );
        }
    }

    @Override
    public Optional<String> findStudyUnitXmlByOperationIri(
        String operationIri
    ) {
        logger.info(
            "Searching StudyUnit XML by operationIri: {}",
            operationIri
        );
        List<PartialStudyUnit> studyUnits = getStudyUnits();
        return studyUnits
            .stream()
            .map(su -> fetchColecticaItem(su.agency(), su.id(), null))
            .filter(Objects::nonNull)
            .filter(item ->
                studyUnitMatchesOperationIri(item.item(), operationIri)
            )
            .map(ColecticaItemResponse::item)
            .findFirst();
    }

    private boolean studyUnitMatchesOperationIri(
        String xml,
        String operationIri
    ) {
        if (xml == null || xml.isBlank()) return false;
        try {
            DocumentBuilderFactory factory =
                createSecureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                new InputSource(new StringReader(xml))
            );
            NodeList userIdNodes = doc.getElementsByTagNameNS(
                "ddi:reusable:3_3",
                "UserID"
            );
            for (int i = 0; i < userIdNodes.getLength(); i++) {
                String text = userIdNodes.item(i).getTextContent();
                if (operationIri.equals(text != null ? text.trim() : null)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.warn(
                "Failed to parse StudyUnit XML for operationIri check",
                e
            );
            return false;
        }
    }

    private static final String STUDY_UNIT_ITEM_TYPE =
        "30ea0200-7121-4f01-8d21-a931a182b86d";
    private static final String GROUP_ITEM_TYPE =
        "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";

    @Override
    public PhysicalInstanceParents getPhysicalInstanceParents(
        String agencyId,
        String id
    ) {
        return authenticator.executeWithAuth(token -> {
            String url =
                instanceConfiguration.baseApiUrl() +
                "_query/relationship/byobject/descriptions";

            RelationshipBySubjectRequest piRequest =
                new RelationshipBySubjectRequest(
                    List.of(STUDY_UNIT_ITEM_TYPE),
                    new RelationshipBySubjectRequest.TargetItemRef(agencyId, id)
                );
            ColecticaParentRef[] studyUnitItems = restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(piRequest)
                .retrieve()
                .body(ColecticaParentRef[].class);

            ColecticaParentRef studyUnitItem = Arrays.stream(
                studyUnitItems != null
                    ? studyUnitItems
                    : new ColecticaParentRef[0]
            )
                .findFirst()
                .orElseThrow(() ->
                    new RuntimeException(
                        "No study unit found for physical instance " +
                            agencyId +
                            "/" +
                            id
                    )
                );

            RelationshipBySubjectRequest suRequest =
                new RelationshipBySubjectRequest(
                    List.of(GROUP_ITEM_TYPE),
                    new RelationshipBySubjectRequest.TargetItemRef(
                        studyUnitItem.agencyId(),
                        studyUnitItem.identifier()
                    )
                );
            ColecticaParentRef[] groupItems = restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(suRequest)
                .retrieve()
                .body(ColecticaParentRef[].class);

            ColecticaParentRef groupItem = Arrays.stream(
                groupItems != null ? groupItems : new ColecticaParentRef[0]
            )
                .findFirst()
                .orElseThrow(() ->
                    new RuntimeException(
                        "No group found for study unit " +
                            studyUnitItem.agencyId() +
                            "/" +
                            studyUnitItem.identifier()
                    )
                );

            return new PhysicalInstanceParents(
                studyUnitItem.agencyId(),
                studyUnitItem.identifier(),
                groupItem.agencyId(),
                groupItem.identifier()
            );
        });
    }
}
