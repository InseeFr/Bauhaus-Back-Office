package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static javax.xml.XMLConstants.*;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.colectica.client.dto.*;
import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DDIRepositoryImpl implements DDIRepository {

    static final Logger logger = LoggerFactory.getLogger(
        DDIRepositoryImpl.class
    );

    private static final String BAUHAUS_API = "bauhaus-api";

    /** DDI type name "PhysicalInstance", used both as {@code itemTypes()} key and as XML element name/value. */
    private static final String PHYSICAL_INSTANCE = "PhysicalInstance";
    /** DDI type name "CodeList", used both as {@code itemTypes()} key and as XML element name. */
    private static final String CODE_LIST = "CodeList";

    private final String defaultLang;

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaConfiguration colecticaConfiguration;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final DDI4toDDI3ConverterService ddi4ToDdi3Converter;
    private final ColecticaClient colecticaClient;
    private final MutualizedCodeListRefsStrategy mutualizedCodeListRefsProvider;

    public DDIRepositoryImpl(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        DDI3toDDI4ConverterService ddi3ToDdi4Converter,
        DDI4toDDI3ConverterService ddi4ToDdi3Converter,
        ColecticaConfiguration colecticaConfiguration,
        ColecticaClient colecticaClient,
        MutualizedCodeListRefsStrategy mutualizedCodeListRefsProvider
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
        this.colecticaConfiguration = colecticaConfiguration;
        this.colecticaClient = colecticaClient;
        this.mutualizedCodeListRefsProvider = mutualizedCodeListRefsProvider;
        this.defaultLang = colecticaConfiguration.langs().getFirst();
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info(
            "Getting physical instances from Colectica API via HTTP (primary instance)"
        );


            ColecticaResponse response = colecticaClient.query(
                List.of(instanceConfiguration.itemTypes().get(PHYSICAL_INSTANCE)));

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
        
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstancesViaAdvancedQuery() {
        logger.info(
            "Getting physical instances from Colectica API via HTTP (_query/advanced)"
        );

        ColecticaAdvancedResponse response = colecticaClient.queryAdvanced(
            List.of(instanceConfiguration.itemTypes().get(PHYSICAL_INSTANCE)));

        if (response == null || response.results() == null) {
            return List.of();
        }

        return response
            .results()
            .stream()
            .map(item -> {
                String id = item.identifier();
                String label = extractLabelFromAdvancedItem(item);
                Date date = parseAdvancedVersionDate(item);
                String agency = item.agencyId();
                return new PartialPhysicalInstance(id, label, date, agency);
            })
            .toList();
    }

    /**
     * Label of an advanced-query item: prefers the {@code label} property, then {@code dcTitle},
     * picking the default language first (then any non-blank value), and finally falling back to the
     * identifier — mirroring {@link #extractLabelFromItem(ColecticaItem)} for the legacy shape.
     */
    private String extractLabelFromAdvancedItem(ColecticaAdvancedItem item) {
        return firstNonBlankLocalized(item, "label")
            .or(() -> firstNonBlankLocalized(item, "dcTitle"))
            .orElseGet(item::identifier);
    }

    private Optional<String> firstNonBlankLocalized(
        ColecticaAdvancedItem item,
        String propertyKey
    ) {
        if (item.textProperties() == null) {
            return Optional.empty();
        }
        List<LocalizedText> values = item.textProperties().get(propertyKey);
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> preferred = values.stream()
            .filter(v -> defaultLang.equals(v.languageTag()))
            .map(LocalizedText::value)
            .filter(v -> v != null && !v.isBlank())
            .findFirst();
        return preferred.or(() -> values.stream()
            .map(LocalizedText::value)
            .filter(v -> v != null && !v.isBlank())
            .findFirst());
    }

    /**
     * First {@code DateProperties.versionDate} value parsed as a UTC instant truncated to the second.
     * The advanced endpoint emits sub-second precision (e.g. {@code 2026-06-29T14:26:32.961778}); the
     * trailing fraction is intentionally dropped to keep parity with the legacy listing.
     */
    private Date parseAdvancedVersionDate(ColecticaAdvancedItem item) {
        if (item.dateProperties() == null) {
            return null;
        }
        List<String> versionDates = item.dateProperties().get("versionDate");
        if (versionDates == null || versionDates.isEmpty()) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return formatter.parse(versionDates.getFirst());
        } catch (ParseException | NullPointerException _) {
            logger.debug("Impossible to parse {}", versionDates.getFirst());
            return null;
        }
    }

    @Override
    public List<PartialLogicalProduct> getLogicalProducts() {
        logger.info(
            "Getting logical products from Colectica API via HTTP (primary instance)"
        );


            ColecticaResponse response = colecticaClient.query(
                List.of(instanceConfiguration.itemTypes().get("LogicalProduct")));

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
                    return new PartialLogicalProduct(id, label, date, agency);
                })
                .toList();
        
    }

    @Override
    public List<PartialGroup> getGroups() {
        logger.info("Getting groups from Colectica API via HTTP");


            ColecticaResponse response = colecticaClient.query(
                List.of("4bd6eef6-99df-40e6-9b11-5b8f64e5cb23"));

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

            ColecticaItemResponse[] itemResponses =
                colecticaClient.getDescriptions(identifiers);

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


            ColecticaResponse response = colecticaClient.query(
                List.of("30ea0200-7121-4f01-8d21-a931a182b86d"));

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
     * across {@code label} then {@code itemName}, trying default lang, then "en", then any
     * other language. Le libellé ({@code label}) est prioritaire sur le nom technique
     * ({@code itemName}) car c'est lui qu'on affiche dans le sélecteur. No fallback to
     * identifier — empty Optional if no non-blank value exists.
     */
    private Optional<String> extractStrictLabel(ColecticaItem item) {
        return firstNonBlank(item.label())
            .or(() -> firstNonBlank(item.itemName()));
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
                    PHYSICAL_INSTANCE
                )
                .getLength() >
            0
        ) {
            return instanceConfiguration.itemTypes().get(PHYSICAL_INSTANCE);
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
                .getElementsByTagNameNS("ddi:logicalproduct:3_3", CODE_LIST)
                .getLength() >
            0
        ) {
            return instanceConfiguration.itemTypes().get(CODE_LIST);
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

            try {
                ColecticaSetItem[] setItems =
                    colecticaClient.getSet(agencyId, id, null);

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

                ColecticaItemResponse[] itemResponses =
                    colecticaClient.getDescriptions(identifiers);

                if (itemResponses == null || itemResponses.length == 0) {
                    return null;
                }

                // Les CodeList et Category sont volontairement écartées du GET PI :
                // payload réduit + on évite la conversion DDI3 -> DDI4 sur ces items,
                // souvent les plus gros. Le front les charge paresseusement (clic
                // sur une variable + endpoint dédié /codeslists).
                Set<String> excludedTypes = codeListAndCategoryItemTypes();
                List<Ddi3Response.Ddi3Item> ddi3Items = Arrays.stream(
                    itemResponses
                )
                    .filter(item -> !excludedTypes.contains(item.itemType()))
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
                    Ddi4Response.SCHEMA
                );

                logger.info(
                    "Successfully converted Physical Instance to DDI4 format"
                );
                return response;
            } catch (Exception e) {
                throw new RuntimeException("Failed to process DDI response", e);
            }
        
    }

    private Set<String> codeListAndCategoryItemTypes() {
        Map<String, String> types = instanceConfiguration.itemTypes();
        if (types == null) return Set.of();
        return Stream.of(CODE_LIST, "Category")
                .map(types::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id) {

            try {
                ColecticaSetItem[] setItems =
                    colecticaClient.getSet(agencyId, id, null);

                if (setItems == null || setItems.length == 0) {
                    return List.of();
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

                ColecticaItemResponse[] itemResponses =
                    colecticaClient.getDescriptions(identifiers);

                if (itemResponses == null || itemResponses.length == 0) {
                    return List.of();
                }

                Set<String> codeListAndCategoryTypes = codeListAndCategoryItemTypes();
                List<Ddi3Response.Ddi3Item> ddi3Items = Arrays.stream(
                    itemResponses
                )
                    .filter(item -> codeListAndCategoryTypes.contains(item.itemType()))
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

                if (ddi3Items.isEmpty()) {
                    return List.of();
                }

                Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                    new Ddi3Response(null, ddi3Items),
                    Ddi4Response.SCHEMA
                );
                return response != null && response.codeList() != null
                    ? response.codeList()
                    : List.of();
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to fetch CodeLists for PhysicalInstance",
                    e
                );
            }
        
    }

    @Override
    public Ddi4GroupResponse getGroup(String agencyId, String id) {
        logger.info(
            "Fetching DDI4 Group from Colectica API for agencyId: {}, id: {}",
            agencyId,
            id
        );


            try {
                // Fetch the full DDI set (Group + StudyUnits) using the ddiset endpoint.
                // Read raw bytes and decode as UTF-8 explicitly: Colectica omits the charset, so
                // Spring's StringHttpMessageConverter would fall back to ISO-8859-1 and produce
                // mojibake on accented characters.
                logger.info("Fetching full DDI set for Group {}/{}", agencyId, id);
                byte[] ddisetBytes = colecticaClient.getDdiSet(agencyId, id);

                if (ddisetBytes == null || ddisetBytes.length == 0) {
                    logger.error(
                        "Received empty response from Colectica API for ddiset {}/{}",
                        agencyId,
                        id
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
            Ddi4Response.SCHEMA,
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

            // Build updated LogicalRecord(s) with new label if provided
            List<LogicalRecord> updatedLRs = currentDR.logicalRecord();
            if (updatedLRs != null && request.logicalRecordLabel() != null) {
                updatedLRs = updatedLRs.stream()
                    .map(lr -> new LogicalRecord(
                        LogicalRecord.TYPE,
                        lr.urn(),
                        lr.agency(),
                        lr.id(),
                        lr.version(),
                        createLabelWithFallback(lr.label(), request.logicalRecordLabel()),
                        lr.variablesInRecord()
                    ))
                    .toList();
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
                updatedLRs
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

        // When a StudyUnit is supplied (duplication workflow, cf. #1555), attach the PhysicalInstance
        // to it in the same save, so GET .../parents can later resolve its Study & Group.
        List<ColecticaItemResponse> additionalItems = new ArrayList<>();
        if (request.studyUnitId() != null && request.studyUnitAgency() != null) {
            additionalItems.add(
                addPhysicalInstanceReferenceToStudyUnit(
                    request.studyUnitAgency(),
                    request.studyUnitId(),
                    agencyId,
                    id
                )
            );
        }

        // Use updateFullPhysicalInstance to save everything including variables
        updateFullPhysicalInstance(agencyId, id, updatedResponse, additionalItems);
    }

    @Override
    public void updateFullPhysicalInstance(
        String agencyId,
        String id,
        Ddi4Response ddi4Response
    ) {
        updateFullPhysicalInstance(agencyId, id, ddi4Response, List.of());
    }

    /**
     * Same as {@link #updateFullPhysicalInstance(String, String, Ddi4Response)} but allows callers to
     * bundle extra already-built Colectica items in the same atomic save — used by the PATCH flow to
     * attach the PhysicalInstance to a StudyUnit (cf. #1555).
     */
    private void updateFullPhysicalInstance(
        String agencyId,
        String id,
        Ddi4Response ddi4Response,
        List<ColecticaItemResponse> additionalItems
    ) {
        logger.info(
            "Updating full physical instance {}/{} with all DDI objects in Colectica",
            agencyId,
            id
        );


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
                .map(this::toColecticaItem)
                .collect(Collectors.toCollection(ArrayList::new));

            // File the physical instance's non-mutualized code lists and categories under the group's
            // schemes, and its variables under the study unit's VariableScheme (auto-provisioning the
            // schemes when they do not exist yet). Parents are resolved once and shared.
            appendSchemeUpdates(agencyId, id, ddi4Response, colecticaItems, additionalItems);

            // Bundle any extra items (e.g. the StudyUnit re-registered with a new PI reference)
            colecticaItems.addAll(additionalItems);

            // Create request with all items
            ColecticaCreateItemRequest updateRequest =
                new ColecticaCreateItemRequest(colecticaItems);

            // Send to Colectica
            logger.info(
                "Sending full update request to Colectica with {} items",
                colecticaItems.size()
            );

            colecticaClient.createOrUpdateItems(updateRequest);

            logger.info(
                "Successfully updated full physical instance with id: {} ({} items saved)",
                id,
                colecticaItems.size()
            );
    }

    private ColecticaItemResponse toColecticaItem(Ddi3Response.Ddi3Item ddi3Item) {
        return new ColecticaItemResponse(
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
        );
    }

    /**
     * Files the physical instance's non-mutualized code lists and categories under the schemes of its
     * group, and its variables under the VariableScheme of its study unit — auto-provisioning any
     * scheme (and its LogicalProduct) that does not exist yet. Every produced item is appended to
     * {@code colecticaItems} so it is saved in the same atomic batch as the physical instance.
     *
     * <p>The parents (PhysicalInstance → StudyUnit → Group) are resolved once and only when there is
     * something to file, preserving the previous behaviour for physical instances whose code lists are
     * all mutualized (no parent resolution, nothing appended).
     */
    private void appendSchemeUpdates(
        String agencyId,
        String id,
        Ddi4Response ddi4Response,
        List<ColecticaItemResponse> colecticaItems,
        List<ColecticaItemResponse> additionalItems
    ) {
        List<Ddi4CodeList> codeLists = ddi4Response.codeList();
        List<Ddi4CodeList> nonMutualized = (codeLists == null || codeLists.isEmpty())
            ? List.of() : filterNonMutualizedCodeLists(codeLists);
        List<Ddi4Category> categories = ddi4Response.category() != null ? ddi4Response.category() : List.of();
        List<Ddi4Variable> variables = ddi4Response.variable() != null ? ddi4Response.variable() : List.of();

        boolean groupWork = !nonMutualized.isEmpty() || !categories.isEmpty();
        boolean studyUnitWork = !variables.isEmpty();
        if (!groupWork && !studyUnitWork) {
            return;
        }

        PhysicalInstanceParents parents = getPhysicalInstanceParents(agencyId, id);
        if (groupWork) {
            appendGroupSchemesUpdate(parents, nonMutualized, categories, colecticaItems);
        }
        if (studyUnitWork) {
            appendStudyUnitVariableSchemeUpdate(parents, variables, colecticaItems, additionalItems);
        }
    }

    /**
     * Files the group's non-mutualized code lists (under its CodeListScheme) and categories (under its
     * CategoryScheme): for each scheme reachable via {@code Group → LogicalProduct → scheme}, the new
     * references are merged into it; when a scheme does not exist yet, a fresh scheme + LogicalProduct
     * are created. The group is re-registered <em>exactly once</em>, carrying every new LogicalProduct.
     */
    private void appendGroupSchemesUpdate(
        PhysicalInstanceParents parents,
        List<Ddi4CodeList> nonMutualized,
        List<Ddi4Category> categories,
        List<ColecticaItemResponse> colecticaItems
    ) {
        String groupAgency = parents.groupAgency();
        String groupId = parents.groupId();
        List<Reference> newLogicalProductRefs = new ArrayList<>();

        if (!nonMutualized.isEmpty()) {
            fileGroupCodeLists(groupAgency, groupId, nonMutualized, colecticaItems, newLogicalProductRefs);
        }
        if (!categories.isEmpty()) {
            fileGroupCategories(groupAgency, groupId, categories, colecticaItems, newLogicalProductRefs);
        }
        if (!newLogicalProductRefs.isEmpty()) {
            reRegisterGroupWithLogicalProducts(groupAgency, groupId, newLogicalProductRefs, colecticaItems);
        }
    }

    private void fileGroupCodeLists(
        String groupAgency, String groupId, List<Ddi4CodeList> nonMutualized,
        List<ColecticaItemResponse> colecticaItems, List<Reference> newLogicalProductRefs
    ) {
        List<Reference> newRefs = nonMutualized.stream()
            .map(cl -> Reference.of(cl.agency(), cl.id(), cl.version(), Ddi4CodeList.TYPE))
            .toList();
        Optional<ItemReference> schemeRefOpt = findContainerScheme(groupAgency, groupId, "CodeListScheme");
        if (schemeRefOpt.isPresent()) {
            ItemReference schemeRef = schemeRefOpt.get();
            Ddi4CodeListScheme current = ddi3ToDdi4Converter.toCodeListScheme(
                colecticaClient.getItem(schemeRef.agencyId(), schemeRef.identifier(), null).item());
            List<Reference> merged = mergeReferences(current.codeListReference(), newRefs);
            if (merged == null) {
                return;
            }
            Ddi4CodeListScheme updated = new Ddi4CodeListScheme(current.type(), current.versionDate(),
                current.urn(), current.agency(), current.id(), current.version(), current.label(), merged);
            colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toCodeListSchemeItem(updated)));
            logger.info("Filed {} code list(s) under code list scheme {}/{} of group {}/{}",
                newRefs.size(), schemeRef.agencyId(), schemeRef.identifier(), groupAgency, groupId);
            return;
        }
        String schemeId = AbstractColecticaItemRepository.generateDeterministicUuid(
            groupAgency + "/" + groupId + "#codelistscheme");
        Ddi4CodeListScheme scheme = new Ddi4CodeListScheme(Ddi4CodeListScheme.TYPE,
            CogsDate.ofDateTime(nowIso()), "urn:ddi:%s:%s:1".formatted(groupAgency, schemeId),
            groupAgency, schemeId, "1", LangStrings.of(defaultLang, "Code List Scheme"), newRefs);
        colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toCodeListSchemeItem(scheme)));
        newLogicalProductRefs.add(appendSchemeLogicalProduct(
            groupAgency, groupId, "#codelist-logicalproduct", schemeId, "CodeListScheme", colecticaItems));
        logger.info("Auto-provisioned code list scheme {}/{} for group {}/{} and filed {} code list(s)",
            groupAgency, schemeId, groupAgency, groupId, newRefs.size());
    }

    private void fileGroupCategories(
        String groupAgency, String groupId, List<Ddi4Category> categories,
        List<ColecticaItemResponse> colecticaItems, List<Reference> newLogicalProductRefs
    ) {
        List<Reference> newRefs = categories.stream()
            .map(cat -> Reference.of(cat.agency(), cat.id(), cat.version(), Ddi4Category.TYPE))
            .toList();
        Optional<ItemReference> schemeRefOpt = findContainerScheme(groupAgency, groupId, "CategoryScheme");
        if (schemeRefOpt.isPresent()) {
            ItemReference schemeRef = schemeRefOpt.get();
            Ddi4CategoryScheme current = ddi3ToDdi4Converter.toCategoryScheme(
                colecticaClient.getItem(schemeRef.agencyId(), schemeRef.identifier(), null).item());
            List<Reference> merged = mergeReferences(current.categoryReference(), newRefs);
            if (merged == null) {
                return;
            }
            Ddi4CategoryScheme updated = new Ddi4CategoryScheme(current.type(), current.versionDate(),
                current.urn(), current.agency(), current.id(), current.version(), current.label(), merged);
            colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toCategorySchemeItem(updated)));
            logger.info("Filed {} category(ies) under category scheme {}/{} of group {}/{}",
                newRefs.size(), schemeRef.agencyId(), schemeRef.identifier(), groupAgency, groupId);
            return;
        }
        String schemeId = AbstractColecticaItemRepository.generateDeterministicUuid(
            groupAgency + "/" + groupId + "#categoryscheme");
        Ddi4CategoryScheme scheme = new Ddi4CategoryScheme(Ddi4CategoryScheme.TYPE,
            CogsDate.ofDateTime(nowIso()), "urn:ddi:%s:%s:1".formatted(groupAgency, schemeId),
            groupAgency, schemeId, "1", LangStrings.of(defaultLang, "Category Scheme"), newRefs);
        colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toCategorySchemeItem(scheme)));
        newLogicalProductRefs.add(appendSchemeLogicalProduct(
            groupAgency, groupId, "#category-logicalproduct", schemeId, "CategoryScheme", colecticaItems));
        logger.info("Auto-provisioned category scheme {}/{} for group {}/{} and filed {} category(ies)",
            groupAgency, schemeId, groupAgency, groupId, newRefs.size());
    }

    /**
     * Keeps only the code lists that are not mutualized, i.e. not part of the configured mutualized
     * codes package tree. When no mutualized package is configured, every code list is considered
     * non-mutualized.
     *
     * <p>Membership is tested against the (cached) set of CodeLists collected by walking the package
     * top-down — the same source of truth as {@link #getMutualizedCodesLists()} — so a single bounded
     * walk replaces the per-code-list upward parent chase, and warm calls cost no HTTP at all.
     */
    private List<Ddi4CodeList> filterNonMutualizedCodeLists(List<Ddi4CodeList> codeLists) {
        if (colecticaConfiguration.mutualizedCodesPackage() == null) {
            return codeLists;
        }
        Set<String> mutualizedKeys = mutualizedCodeListRefsProvider.codeListRefs().stream()
            .map(ref -> ref.agencyId() + "/" + ref.identifier())
            .collect(Collectors.toSet());
        return codeLists.stream()
            .filter(cl -> !mutualizedKeys.contains(cl.agency() + "/" + cl.id()))
            .toList();
    }

    /**
     * Files the physical instance's variables under the VariableScheme of its study unit. If the study
     * unit already exposes a VariableScheme (via {@code StudyUnit → LogicalProduct → VariableScheme}),
     * the new variable references are merged into it; otherwise a fresh VariableScheme + LogicalProduct
     * are created and the study unit is re-registered pointing at it.
     *
     * <p>The re-registration is skipped (and logged) when the study unit is already being re-registered
     * in the same batch — e.g. the duplication flow attaching the physical instance to it — to avoid
     * emitting two conflicting StudyUnit items.
     */
    private void appendStudyUnitVariableSchemeUpdate(
        PhysicalInstanceParents parents,
        List<Ddi4Variable> variables,
        List<ColecticaItemResponse> colecticaItems,
        List<ColecticaItemResponse> additionalItems
    ) {
        String suAgency = parents.studyUnitAgency();
        String suId = parents.studyUnitId();
        List<Reference> newRefs = variables.stream()
            .map(v -> Reference.of(v.agency(), v.id(), v.version(), Ddi4Variable.TYPE))
            .toList();

        Optional<ItemReference> schemeRefOpt = findContainerScheme(suAgency, suId, "VariableScheme");
        if (schemeRefOpt.isPresent()) {
            ItemReference schemeRef = schemeRefOpt.get();
            Ddi4VariableScheme current = ddi3ToDdi4Converter.toVariableScheme(
                colecticaClient.getItem(schemeRef.agencyId(), schemeRef.identifier(), null).item());
            List<Reference> merged = mergeReferences(current.variableReference(), newRefs);
            if (merged == null) {
                return;
            }
            Ddi4VariableScheme updated = new Ddi4VariableScheme(current.type(), current.versionDate(),
                current.urn(), current.agency(), current.id(), current.version(), current.label(), merged);
            colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toVariableSchemeItem(updated)));
            logger.info("Filed {} variable(s) under variable scheme {}/{} of study unit {}/{}",
                newRefs.size(), schemeRef.agencyId(), schemeRef.identifier(), suAgency, suId);
            return;
        }

        if (isStudyUnitAlreadyInBatch(additionalItems, suAgency, suId)) {
            logger.warn("Skipping variable scheme auto-provision for study unit {}/{}: it is already "
                + "re-registered in the same batch; its variables will not be filed this time", suAgency, suId);
            return;
        }

        String schemeId = AbstractColecticaItemRepository.generateDeterministicUuid(
            suAgency + "/" + suId + "#variablescheme");
        Ddi4VariableScheme scheme = new Ddi4VariableScheme(Ddi4VariableScheme.TYPE,
            CogsDate.ofDateTime(nowIso()), "urn:ddi:%s:%s:1".formatted(suAgency, schemeId),
            suAgency, schemeId, "1", LangStrings.of(defaultLang, "Variable Scheme"), newRefs);
        colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toVariableSchemeItem(scheme)));

        Reference logicalProductRef = appendSchemeLogicalProduct(
            suAgency, suId, "#studyunit-logicalproduct", schemeId, "VariableScheme", colecticaItems);

        ColecticaItemResponse suResponse = colecticaClient.getItem(suAgency, suId, null);
        Ddi4StudyUnit studyUnit = ddi3ToDdi4Converter.toStudyUnit(suResponse.item());
        List<Reference> logicalProductRefs = new ArrayList<>(
            studyUnit.logicalProductReferences() != null ? studyUnit.logicalProductReferences() : List.of());
        logicalProductRefs.add(logicalProductRef);
        Ddi4StudyUnit updatedStudyUnit = new Ddi4StudyUnit(studyUnit.type(), studyUnit.versionDate(),
            studyUnit.urn(), studyUnit.agency(), studyUnit.id(), studyUnit.version(), studyUnit.citation(),
            studyUnit.operationIri(), studyUnit.physicalInstanceReferences(), logicalProductRefs);
        colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toStudyUnitItem(updatedStudyUnit, STUDY_UNIT_ITEM_TYPE)));

        logger.info("Auto-provisioned variable scheme {}/{} for study unit {}/{} and filed {} variable(s)",
            suAgency, schemeId, suAgency, suId, newRefs.size());
    }

    /**
     * Finds a scheme of a container (Group or StudyUnit) by walking
     * {@code container → LogicalProduct → scheme} via {@code bysubject} relationships. Returns empty
     * when none exists yet (the caller then auto-provisions one).
     * @param schemeTypeKey the {@code itemTypes} key of the wanted scheme
     *                      (e.g. {@code "CodeListScheme"}, {@code "CategoryScheme"}, {@code "VariableScheme"})
     */
    private Optional<ItemReference> findContainerScheme(String containerAgency, String containerId, String schemeTypeKey) {
        String logicalProductType = instanceConfiguration.itemTypes().get("LogicalProduct");
        String schemeType = instanceConfiguration.itemTypes().get(schemeTypeKey);
        for (ItemReference logicalProduct : colecticaClient.findRelatedDescriptions(
                RelationshipDirection.BY_SUBJECT,
                new ItemReference(containerAgency, containerId),
                List.of(logicalProductType))) {
            Optional<ItemReference> scheme = colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_SUBJECT,
                    logicalProduct,
                    List.of(schemeType))
                .stream()
                .findFirst();
            if (scheme.isPresent()) {
                return scheme;
            }
        }
        return Optional.empty();
    }

    /**
     * Merges {@code toAdd} references into {@code existing} (deduplicated by {@code agency/id}),
     * preserving the ones already present. Returns the merged list, or {@code null} when nothing new
     * was added (so the caller can skip re-saving an unchanged scheme).
     */
    private List<Reference> mergeReferences(List<Reference> existing, List<Reference> toAdd) {
        List<Reference> merged = new ArrayList<>(existing != null ? existing : List.of());
        Set<String> keys = merged.stream().map(r -> r.agency() + "/" + r.id()).collect(Collectors.toSet());
        boolean changed = false;
        for (Reference ref : toAdd) {
            if (keys.add(ref.agency() + "/" + ref.id())) {
                merged.add(ref);
                changed = true;
            }
        }
        return changed ? merged : null;
    }

    /**
     * Creates and appends a LogicalProduct (deterministic id from the container + {@code lpSeedSuffix})
     * that files the just-created scheme {@code schemeId} of type {@code schemeTypeKey}, and returns a
     * reference to it (to be added to the container by its re-registration).
     */
    private Reference appendSchemeLogicalProduct(
        String containerAgency, String containerId, String lpSeedSuffix,
        String schemeId, String schemeTypeKey, List<ColecticaItemResponse> colecticaItems
    ) {
        String logicalProductId = AbstractColecticaItemRepository.generateDeterministicUuid(
            containerAgency + "/" + containerId + lpSeedSuffix);
        Reference schemeReference = Reference.of(containerAgency, schemeId, "1", schemeTypeKey);
        Ddi4LogicalProduct logicalProduct = new Ddi4LogicalProduct(
            Ddi4LogicalProduct.TYPE, CogsDate.ofDateTime(nowIso()),
            "urn:ddi:%s:%s:1".formatted(containerAgency, logicalProductId), containerAgency, logicalProductId, "1",
            LangStrings.of(defaultLang, "Logical Product"),
            "CodeListScheme".equals(schemeTypeKey) ? List.of(schemeReference) : null,
            "CategoryScheme".equals(schemeTypeKey) ? List.of(schemeReference) : null,
            "VariableScheme".equals(schemeTypeKey) ? List.of(schemeReference) : null);
        colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toLogicalProductItem(logicalProduct)));
        return Reference.of(containerAgency, logicalProductId, "1", "LogicalProduct");
    }

    /**
     * Re-registers the group (RegisterOrReplace, same version) with {@code newLogicalProductRefs}
     * appended to its existing LogicalProductReferences, establishing the
     * {@code Group → LogicalProduct → scheme} chain the read paths walk.
     */
    private void reRegisterGroupWithLogicalProducts(
        String groupAgency, String groupId,
        List<Reference> newLogicalProductRefs, List<ColecticaItemResponse> colecticaItems
    ) {
        Ddi4Group group = ddi3ToDdi4Converter.toGroup(
            colecticaClient.getItem(groupAgency, groupId, null).item());
        List<Reference> logicalProductRefs = new ArrayList<>(
            group.logicalProductReference() != null ? group.logicalProductReference() : List.of());
        logicalProductRefs.addAll(newLogicalProductRefs);
        Ddi4Group updatedGroup = new Ddi4Group(
            group.type(), group.versionDate(), group.urn(), group.agency(), group.id(), group.version(),
            group.versionResponsibility(), group.citation(), group.studyUnitReference(),
            group.seriesIris(), group.typeOfGroup(), logicalProductRefs);
        colecticaItems.add(toColecticaItem(ddi4ToDdi3Converter.toGroupItem(updatedGroup, GROUP_ITEM_TYPE)));
    }

    private boolean isStudyUnitAlreadyInBatch(
        List<ColecticaItemResponse> additionalItems, String suAgency, String suId
    ) {
        return additionalItems.stream().anyMatch(item ->
            STUDY_UNIT_ITEM_TYPE.equals(item.itemType())
                && suAgency.equals(item.agencyId())
                && suId.equals(item.identifier()));
    }

    private static String nowIso() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Override
    public Ddi4Response createPhysicalInstance(
        CreatePhysicalInstanceRequest request
    ) {

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
                    instanceConfiguration.itemTypes().get(PHYSICAL_INSTANCE),
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
            colecticaClient.createOrUpdateItems(createRequest);

            // Return the created instance
            return getPhysicalInstance(agencyId, physicalInstanceId);

    }

    @Override
    public void createLogicalProduct(Ddi4LogicalProduct logicalProduct) {
        logger.info("Creating logical product in Colectica: {}/{}", logicalProduct.agency(), logicalProduct.id());
        colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(
            List.of(toColecticaItem(ddi4ToDdi3Converter.toLogicalProductItem(logicalProduct)))));
    }

    @Override
    public void createCodeListScheme(Ddi4CodeListScheme codeListScheme) {
        logger.info("Creating code list scheme in Colectica: {}/{}", codeListScheme.agency(), codeListScheme.id());
        colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(
            List.of(toColecticaItem(ddi4ToDdi3Converter.toCodeListSchemeItem(codeListScheme)))));
    }

    @Override
    public void createCategoryScheme(Ddi4CategoryScheme categoryScheme) {
        logger.info("Creating category scheme in Colectica: {}/{}", categoryScheme.agency(), categoryScheme.id());
        colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(
            List.of(toColecticaItem(ddi4ToDdi3Converter.toCategorySchemeItem(categoryScheme)))));
    }

    @Override
    public void createVariableScheme(Ddi4VariableScheme variableScheme) {
        logger.info("Creating variable scheme in Colectica: {}/{}", variableScheme.agency(), variableScheme.id());
        colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(
            List.of(toColecticaItem(ddi4ToDdi3Converter.toVariableSchemeItem(variableScheme)))));
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
        return getCodeList(agencyId, id, null);
    }

    /**
     * Returns the full DDI4 representation of one code list (codes + categories) for an optional
     * {@code version} (latest when null), via the {@code set/} + {@code _getList} + DDI3→DDI4
     * conversion pipeline. Backs both {@code getMutualizedCodesList} (#485, latest) and the
     * versioned {@code /codelist} endpoint.
     */
    @Override
    public Ddi4Response getCodeList(String agencyId, String id, String version) {
        logger.info("Fetching code list {}/{}/{}", agencyId, id, version);


            try {
                ColecticaItemResponse[] itemResponses = fetchSetItems(agencyId, id, version);
                if (itemResponses == null || itemResponses.length == 0
                    || !rootItemHasType(itemResponses, id, CODE_LIST)) {
                    return null;
                }
                Ddi3Response ddi3Response = new Ddi3Response(null, toDdi3Items(itemResponses));
                Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(ddi3Response, Ddi4Response.SCHEMA);
                return withTopLevelReference(response, findTopLevelReference(itemResponses, "CodeList"));
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to fetch code list " + agencyId + "/" + id + "/" + version, e);
            }
        
    }

    /**
     * Returns the DDI 3.3 representation of a code list set (CodeList + its referenced Categories)
     * as a single multi-fragment {@code <FragmentInstance>} (#485).
     */
    @Override
    public String getCodeListXml(String agencyId, String id, String version) {
        logger.info("Fetching code list XML {}/{}/{}", agencyId, id, version);


            try {
                ColecticaItemResponse[] itemResponses = fetchSetItems(agencyId, id, version);
                if (itemResponses == null || itemResponses.length == 0
                    || !rootItemHasType(itemResponses, id, CODE_LIST)) {
                    return null;
                }
                return assembleFragmentInstance(fragmentXmls(Arrays.stream(itemResponses).toList()));
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to fetch code list XML " + agencyId + "/" + id + "/" + version, e);
            }
        
    }

    /**
     * Fetches the items of a Colectica set ({@code set/} → {@code item/_getList}) for an optional
     * {@code version} appended to the set URL (latest when null). Shared by the code list and
     * data relationship accessors.
     */
    private ColecticaItemResponse[] fetchSetItems(String agencyId, String id, String version) {
        ColecticaSetItem[] setItems = colecticaClient.getSet(agencyId, id, version);

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

        return colecticaClient.getDescriptions(identifiers);
    }

    /**
     * Vérifie que l'item racine {@code id} du set est bien du type attendu (#493). La racine figure
     * toujours parmi les {@link ColecticaItemResponse} remontés par {@link #fetchSetItems}, donc la
     * validation ne coûte aucun appel réseau supplémentaire (Option B).
     *
     * <p>Renvoie {@code false} quand la racine est absente <em>ou</em> d'un autre type : les accesseurs
     * retournent alors {@code null}, converti en {@code 404} côté contrôleur. Le 404 unique (mauvais
     * type comme inexistant) ne divulgue pas l'existence d'un objet d'un type différent.
     *
     * @param expectedTypeKey clé de {@link ColecticaConfiguration.ColecticaInstanceConfiguration#itemTypes()}
     *                        (p.ex. {@code "CodeList"} ou {@code "PhysicalInstance"})
     */
    private boolean rootItemHasType(ColecticaItemResponse[] itemResponses, String id, String expectedTypeKey) {
        String expectedType = instanceConfiguration.itemTypes().get(expectedTypeKey);
        return Arrays.stream(itemResponses)
            .filter(item -> Objects.equals(item.identifier(), id))
            .findFirst()
            .map(item -> Objects.equals(item.itemType(), expectedType))
            .orElse(false);
    }

    private List<Ddi3Response.Ddi3Item> toDdi3Items(ColecticaItemResponse[] itemResponses) {
        return Arrays.stream(itemResponses)
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
    }

    private List<String> fragmentXmls(List<ColecticaItemResponse> items) {
        return items.stream()
            .map(ColecticaItemResponse::item)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Wraps DDI 3.3 {@code <Fragment>} documents (as returned by Colectica) into a single
     * {@code <FragmentInstance>}. Each fragment already declares its own namespaces, so leading
     * XML declarations are stripped before concatenation.
     */
    private String assembleFragmentInstance(List<String> fragmentXmls) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<ddi:FragmentInstance xmlns:ddi=\"ddi:instance:3_3\" xmlns:r=\"ddi:reusable:3_3\">\n");
        for (String fragmentXml : fragmentXmls) {
            sb.append(stripXmlDeclaration(fragmentXml)).append("\n");
        }
        sb.append("</ddi:FragmentInstance>");
        return sb.toString();
    }

    private String stripXmlDeclaration(String xml) {
        String trimmed = xml.stripLeading();
        if (trimmed.startsWith("<?xml")) {
            int end = trimmed.indexOf("?>");
            if (end >= 0) {
                return trimmed.substring(end + 2).stripLeading();
            }
        }
        return trimmed;
    }

    /**
     * Builds the {@code TopLevelReference} of a FragmentInstance from the set item whose type matches
     * {@code typeKey} (#494). Endpoints like {@code /variables} and {@code /codelist} drop their root
     * item from the converted output, so the converter alone leaves the reference null; we recover it
     * from the raw item descriptions (which still carry the root item with its resolved version).
     * Returns {@code null} when the item type configuration or the matching item is missing.
     */
    private Reference findTopLevelReference(ColecticaItemResponse[] itemResponses, String typeKey) {
        Map<String, String> types = instanceConfiguration.itemTypes();
        if (types == null) {
            return null;
        }
        String typeUuid = types.get(typeKey);
        if (typeUuid == null) {
            return null;
        }
        return Arrays.stream(itemResponses)
            .filter(item -> Objects.equals(item.itemType(), typeUuid))
            .findFirst()
            .map(item -> Reference.of(
                item.agencyId(), item.identifier(), String.valueOf(item.version()), typeKey))
            .orElse(null);
    }

    /**
     * Returns {@code response} with its {@code TopLevelReference} set to {@code topLevelReference},
     * leaving an already-populated reference untouched. No-op when either argument is null.
     */
    private Ddi4Response withTopLevelReference(Ddi4Response response, Reference topLevelReference) {
        if (response == null || topLevelReference == null
                || (response.topLevelReference() != null && !response.topLevelReference().isEmpty())) {
            return response;
        }
        return new Ddi4Response(
            response.schema(),
            List.of(topLevelReference),
            response.physicalInstance(),
            response.dataRelationship(),
            response.variable(),
            response.codeList(),
            response.category());
    }

    /**
     * Returns every DataRelationship of a PhysicalInstance set as DDI4 (#447), latest version when
     * {@code version} is null. The DataRelationship fragments and the Variable fragments they
     * reference are converted; the referenced CodeList/Category are left out.
     */
    @Override
    public Ddi4Response getDataRelationships(String agencyId, String id, String version) {
        logger.info("Fetching data relationships {}/{}/{}", agencyId, id, version);


            try {
                ColecticaItemResponse[] itemResponses = fetchSetItems(agencyId, id, version);
                if (itemResponses == null || itemResponses.length == 0
                    || !rootItemHasType(itemResponses, id, PHYSICAL_INSTANCE)) {
                    return null;
                }
                ColecticaItemResponse[] dataRelationships = filterDataRelationshipsAndVariables(itemResponses);
                Ddi3Response ddi3Response = new Ddi3Response(null, toDdi3Items(dataRelationships));
                Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(ddi3Response, Ddi4Response.SCHEMA);
                return withTopLevelReference(response, findTopLevelReference(itemResponses, "PhysicalInstance"));
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to fetch data relationships " + agencyId + "/" + id + "/" + version, e);
            }
        
    }

    /**
     * Returns every DataRelationship of a PhysicalInstance set and the Variable fragments they
     * reference as a single DDI 3.3 multi-fragment {@code <FragmentInstance>} (#447), latest version
     * when {@code version} is null. The referenced CodeList/Category are left out.
     */
    @Override
    public String getDataRelationshipsXml(String agencyId, String id, String version) {
        logger.info("Fetching data relationships XML {}/{}/{}", agencyId, id, version);


            try {
                ColecticaItemResponse[] itemResponses = fetchSetItems(agencyId, id, version);
                if (itemResponses == null || itemResponses.length == 0
                    || !rootItemHasType(itemResponses, id, PHYSICAL_INSTANCE)) {
                    return null;
                }
                ColecticaItemResponse[] dataRelationships = filterDataRelationshipsAndVariables(itemResponses);
                return assembleFragmentInstance(fragmentXmls(Arrays.stream(dataRelationships).toList()));
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to fetch data relationships XML " + agencyId + "/" + id + "/" + version, e);
            }
        
    }

    /**
     * Keeps the DataRelationship fragments of the set and the Variable fragments referenced by their
     * {@code VariablesInRecord}/{@code VariableUsedReference} (#447). In a PhysicalInstance set the
     * Variable items are exactly the ones used by the data relationships, so a type filter is
     * sufficient. The referenced CodeList/Category are deliberately left out.
     */
    private ColecticaItemResponse[] filterDataRelationshipsAndVariables(ColecticaItemResponse[] itemResponses) {
        Map<String, String> types = instanceConfiguration.itemTypes();
        String dataRelationshipType = types.get("DataRelationship");
        String variableType = types.get("Variable");
        return Arrays.stream(itemResponses)
            .filter(item -> Objects.equals(item.itemType(), dataRelationshipType)
                         || Objects.equals(item.itemType(), variableType))
            .toArray(ColecticaItemResponse[]::new);
    }

    /**
     * Returns every LogicalProduct directly referenced by the group {@code agencyId/groupId}.
     * We ask Colectica for the group's {@code bysubject} relationships filtered server-side to the
     * LogicalProduct item type ({@code _query/relationship/bysubject/descriptions}), which returns
     * lightweight references only — avoiding the full {@code set/} + {@code _getList} download that
     * would fetch every item (code lists, categories…) just to read its type. Labels are then
     * resolved through the repository-wide {@link #getLogicalProducts} query (the relationship
     * descriptions carry no label).
     */
    @Override
    public List<PartialLogicalProduct> getLogicalProductsByGroup(String agencyId, String groupId) {
        logger.info("Fetching logical products for group {}/{}", agencyId, groupId);
        String logicalProductType = instanceConfiguration.itemTypes().get("LogicalProduct");

        Set<String> logicalProductIds = colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_SUBJECT,
                    new ItemReference(agencyId, groupId),
                    List.of(logicalProductType))
                .stream()
                .map(ItemReference::identifier)
                .collect(Collectors.toSet());

        if (logicalProductIds.isEmpty()) {
            return List.of();
        }

        return getLogicalProducts().stream()
            .filter(lp -> logicalProductIds.contains(lp.id()))
            .toList();
    }

    /**
     * Returns every CodeListScheme directly referenced by the logical product {@code agencyId/logicalProductId}.
     * Same lightweight strategy as {@link #getLogicalProductsByGroup}: a server-side type-filtered
     * {@code bysubject} relationship query yields the referenced identifiers, then labels are
     * resolved through the repository-wide {@link #getCodeListSchemes} query.
     */
    @Override
    public List<PartialCodeListScheme> getCodeListSchemesByLogicalProduct(String agencyId, String logicalProductId) {
        logger.info("Fetching code list schemes for logical product {}/{}", agencyId, logicalProductId);
        String codeListSchemeType = instanceConfiguration.itemTypes().get("CodeListScheme");

        Set<String> codeListSchemeIds = colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_SUBJECT,
                    new ItemReference(agencyId, logicalProductId),
                    List.of(codeListSchemeType))
                .stream()
                .map(ItemReference::identifier)
                .collect(Collectors.toSet());

        if (codeListSchemeIds.isEmpty()) {
            return List.of();
        }

        return getCodeListSchemes().stream()
            .filter(scheme -> codeListSchemeIds.contains(scheme.id()))
            .toList();
    }

    /**
     * Returns every CodeList directly referenced by the code list scheme {@code agencyId/codeListSchemeId}.
     * Same lightweight strategy as {@link #getCodeListSchemesByLogicalProduct}: a server-side type-filtered
     * {@code bysubject} relationship query yields the referenced identifiers, then labels are resolved
     * through the repository-wide {@link #getCodeLists} query.
     */
    @Override
    public List<PartialCodesList> getCodeListsByCodeListScheme(String agencyId, String codeListSchemeId) {
        logger.info("Fetching code lists for code list scheme {}/{}", agencyId, codeListSchemeId);
        String codeListType = instanceConfiguration.itemTypes().get(CODE_LIST);

        Set<String> codeListIds = colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_SUBJECT,
                    new ItemReference(agencyId, codeListSchemeId),
                    List.of(codeListType))
                .stream()
                .map(ItemReference::identifier)
                .collect(Collectors.toSet());

        if (codeListIds.isEmpty()) {
            return List.of();
        }

        // Libellés via un _query global (les descriptions de relation ne les portent pas), restreint
        // aux CodeLists référencées par le scheme.
        ColecticaResponse response = colecticaClient.query(List.of(codeListType));
        List<ColecticaItem> keptItems = (response == null || response.results() == null)
            ? List.of()
            : response.results().stream()
                .filter(item -> item != null && codeListIds.contains(item.identifier()))
                .toList();
        if (keptItems.isEmpty()) {
            return List.of();
        }

        // versionDate lu depuis le XML de l'item (l'enveloppe _query n'est pas fiable), comme pour
        // les code lists mutualisées.
        Map<String, ColecticaItem> itemsByKey = new LinkedHashMap<>();
        List<ItemReference> refs = new ArrayList<>();
        for (ColecticaItem item : keptItems) {
            itemsByKey.put(item.agencyId() + "/" + item.identifier(), item);
            refs.add(new ItemReference(item.agencyId(), item.identifier()));
        }
        Map<String, Date> versionDateByKey = fetchVersionDatesFromItemXml(refs, itemsByKey);

        return keptItems.stream()
            .map(item -> new PartialCodesList(
                item.identifier(),
                extractLabelFromItem(item),
                versionDateByKey.get(item.agencyId() + "/" + item.identifier()),
                item.agencyId()))
            .toList();
    }

    /**
     * Returns every Variable that uses the code list {@code codeListAgencyId/codeListId}, paired with the
     * PhysicalInstance it belongs to. Walks the {@code byobject} relationship graph (« who references X »):
     * CodeList ← Variable, Variable ← DataRelationship, DataRelationship ← PhysicalInstance.
     */
    @Override
    public List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId) {
        logger.info("Fetching variables using code list {}/{}", codeListAgencyId, codeListId);
        Map<String, String> types = instanceConfiguration.itemTypes();
        String variableType = types.get("Variable");
        String dataRelationshipType = types.get("DataRelationship");
        String physicalInstanceType = types.get(PHYSICAL_INSTANCE);
        String studyUnitType = types.get("StudyUnit");

        // Walk the byobject graph: CodeList ← Variable ← DataRelationship ← PhysicalInstance ← StudyUnit.
        // The {@code /descriptions} endpoint already returns each related item's ItemName/Label, so we use
        // findRelatedItems (which keeps them) for the levels we need labelled — no separate label query
        // and no extra HTTP call. DataRelationships are only intermediate, so bare references suffice.
        List<ColecticaItem> variables = colecticaClient.findRelatedItems(
            RelationshipDirection.BY_OBJECT,
            new ItemReference(codeListAgencyId, codeListId),
            List.of(variableType));
        if (variables.isEmpty()) {
            return List.of();
        }

        List<CodeListVariableUsage> usages = new ArrayList<>();
        for (ColecticaItem variable : variables) {
            List<ItemReference> dataRelationships = colecticaClient.findRelatedDescriptions(
                RelationshipDirection.BY_OBJECT, itemRef(variable), List.of(dataRelationshipType));
            for (ItemReference dataRelationship : dataRelationships) {
                List<ColecticaItem> physicalInstances = colecticaClient.findRelatedItems(
                    RelationshipDirection.BY_OBJECT, dataRelationship, List.of(physicalInstanceType));
                for (ColecticaItem physicalInstance : physicalInstances) {
                    ColecticaItem studyUnit = colecticaClient.findRelatedItems(
                            RelationshipDirection.BY_OBJECT, itemRef(physicalInstance), List.of(studyUnitType))
                        .stream().findFirst().orElse(null);
                    usages.add(new CodeListVariableUsage(
                        studyUnit == null ? null : studyUnit.agencyId(),
                        studyUnit == null ? null : studyUnit.identifier(),
                        studyUnit == null ? null : extractLabelFromItem(studyUnit),
                        physicalInstance.agencyId(),
                        physicalInstance.identifier(),
                        extractLabelFromItem(physicalInstance),
                        variable.agencyId(),
                        variable.identifier(),
                        extractLabelFromItem(variable)));
                }
            }
        }
        return usages.stream().distinct().toList();
    }

    private static ItemReference itemRef(ColecticaItem item) {
        return new ItemReference(item.agencyId(), item.identifier());
    }

    /**
     * Repository-wide {@code _query} of every CodeListScheme, carrying labels (which the relationship
     * descriptions do not). Also used to resolve the labels of the schemes referenced by a logical product.
     */
    @Override
    public List<PartialCodeListScheme> getCodeListSchemes() {
        logger.info("Getting code list schemes from Colectica API via HTTP (primary instance)");


            ColecticaResponse response = colecticaClient.query(
                List.of(instanceConfiguration.itemTypes().get("CodeListScheme")));

            return response
                .results()
                .stream()
                .map(item -> new PartialCodeListScheme(
                    item.identifier(),
                    extractLabelFromItem(item),
                    parseColecticaDate(item.versionDate()),
                    item.agencyId()))
                .toList();
        
    }

    /**
     * Returns metadata for every CodeList reachable from the configured mutualized codes package by
     * walking its tree <em>top-down</em>.
     *
     * <p>Flow:
     * <ol>
     *   <li>From the package, collect its child CodeListSchemes, then their child CodeListGroups,
     *       then those groups' child CodeLists — each step a server-side type-filtered
     *       {@code POST _query/relationship/bysubject/descriptions} ({@code package → CodeListScheme
     *       → CodeListGroup → CodeList}).</li>
     *   <li>The relationship descriptions carry only {@code agency/identifier} (no label), so labels
     *       and version dates are resolved with a single repository-wide {@code POST _query
     *       itemTypes=[CodeList]} mapped by {@code agency/identifier}.</li>
     * </ol>
     *
     * <p>Items are kept only when they carry a non-blank label (no fallback to identifier).
     * Duplicates (a CodeList reachable through several groups) are removed by {@code agency/identifier}.
     *
     * <p>The result is cached through Spring's caching abstraction
     * ({@link ColecticaCacheNames#MUTUALIZED_CODES_LISTS}, TTL configured via
     * {@code mutualized-codes-cache-ttl}) to avoid hitting Colectica on every request.
     */
    @Override
    @Cacheable(ColecticaCacheNames.MUTUALIZED_CODES_LISTS)
    public List<PartialCodesList> getMutualizedCodesLists() {
        List<ItemReference> codeListRefs = mutualizedCodeListRefsProvider.codeListRefs();
        if (codeListRefs.isEmpty()) {
            return List.of();
        }

        // Resolve labels/dates with a single repository-wide CodeList query (the relationship
        // descriptions carry only agency/identifier).
        String codeListType = instanceConfiguration.itemTypes().get(CODE_LIST);
        Map<String, ColecticaItem> itemsByKey = new HashMap<>();
        ColecticaResponse response = colecticaClient.query(List.of(codeListType));
        if (response != null && response.results() != null) {
            for (ColecticaItem item : response.results()) {
                if (item != null) {
                    itemsByKey.put(item.agencyId() + "/" + item.identifier(), item);
                }
            }
        }

        // Le versionDate du _query n'est pas fiable (Colectica renvoie 0001-01-01) : on le lit depuis
        // l'attribut versionDate du XML de l'item, récupéré en un seul item/_getList sur les CodeLists
        // retenues. Résultat amorti par le cache @Cacheable de la méthode.
        Map<String, Date> versionDateByKey = fetchVersionDatesFromItemXml(codeListRefs, itemsByKey);

        Map<String, PartialCodesList> collected = new LinkedHashMap<>();
        for (ItemReference ref : codeListRefs) {
            String key = ref.agencyId() + "/" + ref.identifier();
            ColecticaItem item = itemsByKey.get(key);
            if (item == null) continue;
            Optional<String> label = extractStrictLabel(item);
            if (label.isEmpty()) continue;
            // Nom technique (itemName) conservé à part du libellé : il sert à la recherche
            // dans le sélecteur côté front, où seul le libellé est affiché.
            String name = firstNonBlank(item.itemName()).orElse(null);
            collected.putIfAbsent(key, new PartialCodesList(
                item.identifier(), label.get(), versionDateByKey.get(key), item.agencyId(), name
            ));
        }
        logger.info("{} mutualized CodeList(s) kept", collected.size());

        return List.copyOf(collected.values());
    }

    /**
     * Clears both mutualized caches so the next {@link #getMutualizedCodesLists()} walks Colectica
     * again: the high-level code lists ({@link ColecticaCacheNames#MUTUALIZED_CODES_LISTS}) and the
     * underlying package CodeList references ({@link ColecticaCacheNames#MUTUALIZED_PACKAGE_CODE_LIST_REFS},
     * populated by {@link MutualizedCodeListRefsProvider}). Evicting only the former would still serve
     * a stale package tree on recompute, so both regions are flushed.
     */
    @Override
    @CacheEvict(
        cacheNames = {
            ColecticaCacheNames.MUTUALIZED_CODES_LISTS,
            ColecticaCacheNames.MUTUALIZED_PACKAGE_CODE_LIST_REFS
        },
        allEntries = true)
    public void evictMutualizedCodesListsCache() {
        logger.info("Mutualized codes lists caches evicted");
    }

    /**
     * Batch-fetches the XML of the given code list references (item/_getList) and maps each
     * {@code agency/identifier} to the {@code versionDate} read from its XML. Refs absent from
     * {@code itemsByKey} are skipped (no label resolved). Returns an empty map when nothing is to fetch
     * or the call yields nothing; a ref whose XML has no parsable versionDate maps to a {@code null} date.
     */
    private Map<String, Date> fetchVersionDatesFromItemXml(
        List<ItemReference> codeListRefs,
        Map<String, ColecticaItem> itemsByKey
    ) {
        List<GetDescriptionsRequest.IdentifierRef> identifiers = codeListRefs.stream()
            .map(ref -> itemsByKey.get(ref.agencyId() + "/" + ref.identifier()))
            .filter(Objects::nonNull)
            .map(item -> new GetDescriptionsRequest.IdentifierRef(
                item.agencyId(), item.identifier(), item.version()))
            .distinct()
            .toList();
        if (identifiers.isEmpty()) {
            return Map.of();
        }

        ColecticaItemResponse[] responses = colecticaClient.getDescriptions(identifiers);
        if (responses == null) {
            return Map.of();
        }

        Map<String, Date> versionDateByKey = new HashMap<>();
        for (ColecticaItemResponse response : responses) {
            if (response == null) continue;
            versionDateByKey.put(
                response.agencyId() + "/" + response.identifier(),
                extractVersionDateFromItemXml(response.item()));
        }
        return versionDateByKey;
    }

    /**
     * Reads the {@code versionDate} attribute carried by the (first) DDI element of an item's XML
     * fragment. Returns {@code null} when the XML is blank, unparsable, or carries no such attribute.
     */
    private Date extractVersionDateFromItemXml(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            NodeList elements = doc.getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                String versionDate = ((Element) elements.item(i)).getAttribute("versionDate");
                if (versionDate != null && !versionDate.isBlank()) {
                    return parseColecticaDate(versionDate);
                }
            }
            return null;
        } catch (Exception e) {
            logger.warn("Failed to parse versionDate from item XML", e);
            return null;
        }
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
        return colecticaClient.getItem(agency, id, version);
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
            typeEl.setTextContent(PHYSICAL_INSTANCE);
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
        ColecticaResponse studyUnits = colecticaClient.query(
            List.of(STUDY_UNIT_ITEM_TYPE)
        );
        List<GetDescriptionsRequest.IdentifierRef> identifiers = studyUnits
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
        if (identifiers.isEmpty()) {
            return Optional.empty();
        }
        // Single batch fetch of all StudyUnit XMLs (item/_getList) instead of one
        // HTTP call per StudyUnit, which was the main source of latency here.
        ColecticaItemResponse[] items = colecticaClient.getDescriptions(
            identifiers
        );
        List<String> candidateUserIds = new ArrayList<>();
        for (ColecticaItemResponse item : items) {
            if (item == null) {
                continue;
            }
            List<String> userIds = extractUserIds(item.item());
            candidateUserIds.addAll(userIds);
            if (userIds.contains(operationIri)) {
                return Optional.of(item.item());
            }
        }
        logger.warn(
            "No StudyUnit matched operationIri '{}' among {} study unit(s). Candidate UserIDs found: {}",
            operationIri,
            identifiers.size(),
            candidateUserIds
        );
        return Optional.empty();
    }

    private List<String> extractUserIds(String xml) {
        if (xml == null || xml.isBlank()) return List.of();
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
                if (text != null) {
                    userIds.add(text.trim());
                }
            }
            return userIds;
        } catch (Exception e) {
            logger.warn(
                "Failed to parse StudyUnit XML for operationIri check",
                e
            );
            return List.of();
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

            ItemReference studyUnitItem = colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_OBJECT,
                    new ItemReference(agencyId, id),
                    List.of(STUDY_UNIT_ITEM_TYPE))
                .stream()
                .findFirst()
                .orElseThrow(() ->
                    new RuntimeException(
                        "No study unit found for physical instance " + agencyId + "/" + id
                    )
                );

            ItemReference groupItem = colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_OBJECT,
                    new ItemReference(studyUnitItem.agencyId(), studyUnitItem.identifier()),
                    List.of(GROUP_ITEM_TYPE))
                .stream()
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
        
    }
}
