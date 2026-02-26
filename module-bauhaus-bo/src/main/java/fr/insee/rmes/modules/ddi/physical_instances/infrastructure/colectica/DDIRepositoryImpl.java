package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration.MutualizedCodeListEntry;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static javax.xml.XMLConstants.*;

public class DDIRepositoryImpl implements DDIRepository {
    static final Logger logger = LoggerFactory.getLogger(DDIRepositoryImpl.class);

    private static final String MUTUALIZED_CODE_LIST_UUID = "dc337820-af3a-4c0b-82f9-cf02535cde83";
    private static final String BAUHAUS_API = "bauhaus-api";
    private static final String DEFAULT_LANG = "fr-FR";

    private final RestTemplate restTemplate;
    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaConfiguration colecticaConfiguration;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final DDI4toDDI3ConverterService ddi4ToDdi3Converter;
    private final ColecticaAuthenticator authenticator;
    private Set<String> denyListCache;

    public DDIRepositoryImpl(
            RestTemplate restTemplate,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            DDI4toDDI3ConverterService ddi4ToDdi3Converter,
            ColecticaConfiguration colecticaConfiguration,
            ColecticaAuthenticator authenticator
            ) {
        this.restTemplate = restTemplate;
        this.instanceConfiguration = instanceConfiguration;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
        this.colecticaConfiguration = colecticaConfiguration;
        this.authenticator = authenticator;
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info("Getting physical instances from Colectica API via HTTP (primary instance)");

        return authenticator.executeWithAuth(token -> {
            // Set up the request with authorization header
            String url = instanceConfiguration.baseApiUrl() + "_query";

            // Create request body with itemTypes from configuration
            QueryRequest requestBody = new QueryRequest(List.of(instanceConfiguration.itemTypes().get("PhysicalInstance")));

            // Create headers with Bearer token and Content-Type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            // Create HTTP entity with headers and body
            HttpEntity<QueryRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make the request with authentication
            ColecticaResponse response = restTemplate.postForObject(url, requestEntity, ColecticaResponse.class);

            return response.results().stream()
                    .map(item -> {
                        String id = item.identifier();
                        String label = extractLabelFromItem(item);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
            // Set up the request with authorization header
            String url = instanceConfiguration.baseApiUrl() + "_query";

            // Create request body with Group itemType
            QueryRequest requestBody = new QueryRequest(List.of("4bd6eef6-99df-40e6-9b11-5b8f64e5cb23"));

            // Create headers with Bearer token and Content-Type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            // Create HTTP entity with headers and body
            HttpEntity<QueryRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make the request with authentication
            ColecticaResponse response = restTemplate.postForObject(url, requestEntity, ColecticaResponse.class);

            return response.results().stream()
                    .map(item -> {
                        String id = item.identifier();
                        String label = extractLabelFromItem(item);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = null;
                        try {
                            date = formatter.parse(item.versionDate());
                        } catch (ParseException | NullPointerException _) {
                            logger.debug("Impossible to parse {}", item.versionDate());
                        }
                        String agency = item.agencyId();
                        return new PartialGroup(id, label, date, agency);
                    })
                    .toList();
        });
    }

    @Override
    public List<PartialCodesList> getCodesLists() {
        logger.info("Getting codes lists from Colectica API via HTTP");

        return authenticator.executeWithAuth(token -> {
            // Set up the request with authorization header
            String url = instanceConfiguration.baseApiUrl() + "_query";

            // Create request body with CodeList itemType
            QueryRequest requestBody = new QueryRequest(List.of("8b108ef8-b642-4484-9c49-f88e4bf7cf1d"));

            // Create headers with Bearer token and Content-Type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            // Create HTTP entity with headers and body
            HttpEntity<QueryRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make the request with authentication
            ColecticaResponse response = restTemplate.postForObject(url, requestEntity, ColecticaResponse.class);

            int totalCount = response.results().size();
            logger.debug("Received {} code lists from Colectica API", totalCount);

            List<PartialCodesList> result = response.results().stream()
                    .filter(item -> !isCodeListInDenyList(item.agencyId(), item.identifier()))
                    .map(item -> {
                        String id = item.identifier();
                        String label = extractLabelFromItem(item);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = null;
                        try {
                            date = formatter.parse(item.versionDate());
                        } catch (ParseException | NullPointerException _) {
                            logger.debug("Impossible to parse {}", item.versionDate());
                        }
                        String agency = item.agencyId();
                        return new PartialCodesList(id, label, date, agency);
                    })
                    .toList();

            int filteredCount = totalCount - result.size();
            if (filteredCount > 0) {
                logger.info("Filtered {} code list(s) from {} total using deny list (returned {} code lists)",
                        filteredCount, totalCount, result.size());
            } else {
                logger.debug("No code lists filtered, returning all {} code lists", result.size());
            }

            return result;
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
    
    private String extractLabelFromLanguageMap(Map<String, String> languageMap) {
        if (languageMap == null) {
            return null;
        }

        // Try French first, then English, then first available
        String label = languageMap.get("fr-FR");
        if (label == null || label.trim().isEmpty()) {
            label = languageMap.get("en");
        }
        if (label == null || label.trim().isEmpty()) {
            label = languageMap.values().stream().findFirst().orElse(null);
        }
        return label;
    }

    /**
     * Check if a code list is in the deny list.
     *
     * <p>This method uses a cached HashSet for O(1) lookup performance.
     * The cache is lazily initialized on first access.
     *
     * <p>Code lists matching entries in the deny list (based on agencyId and id)
     * will be excluded from the results. This is useful for filtering out
     * deprecated, test, or otherwise unwanted code lists from the Colectica repository.
     *
     * <p>Configuration example in properties file:
     * <pre>
     * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].agency-id = fr.insee
     * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].id = 2a22ba00-a977-4a61-a582-99025c6b0582
     * </pre>
     *
     * @param agencyId The agency ID of the code list to check
     * @param id The ID of the code list to check
     * @return true if the code list should be filtered out, false otherwise
     * @see ColecticaConfiguration.CodeListDenyEntry
     */
    private boolean isCodeListInDenyList(String agencyId, String id) {
        if (colecticaConfiguration == null || colecticaConfiguration.codeListDenyList() == null) {
            return false;
        }

        // Lazy initialization of cache for O(1) lookups
        if (denyListCache == null) {
            denyListCache = colecticaConfiguration.codeListDenyList().stream()
                    .map(entry -> createDenyListKey(entry.agencyId(), entry.id()))
                    .collect(Collectors.toSet());
            logger.info("Initialized code list deny list cache with {} entries", denyListCache.size());
        }

        String key = createDenyListKey(agencyId, id);
        boolean isDenied = denyListCache.contains(key);

        if (isDenied) {
            logger.debug("Filtering out code list: agencyId={}, id={}", agencyId, id);
        }

        return isDenied;
    }

    /**
     * Creates a unique key for deny list lookups by combining agencyId and id.
     *
     * @param agencyId The agency ID
     * @param id The code list ID
     * @return A unique key string in format "agencyId:id"
     */
    private String createDenyListKey(String agencyId, String id) {
        return agencyId + ":" + id;
    }

    /**
     * Parse FragmentInstance XML and extract each Fragment as a separate Ddi3Item
     * The FragmentInstance contains multiple Fragment elements (PhysicalInstance, DataRelationship, etc.)
     */
    private List<Ddi3Response.Ddi3Item> parseFragmentInstanceToItems(String fragmentInstanceXml, String agencyId, String id) {
        try {
            // Parse the XML with XXE protection
            DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(fragmentInstanceXml)));

            List<Ddi3Response.Ddi3Item> items = new ArrayList<>();

            // Get all Fragment elements
            NodeList fragmentNodes = doc.getElementsByTagNameNS("ddi:instance:3_3", "Fragment");

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
                    MUTUALIZED_CODE_LIST_UUID // itemFormat
                );

                items.add(ddi3Item);
            }

            return items;
        } catch (Exception e) {
            logger.error("Error parsing FragmentInstance XML", e);
            throw new RuntimeException("Failed to parse FragmentInstance XML", e);
        }
    }

    /**
     * Determine the item type based on the content of a Fragment element
     * Returns null if the Fragment type is not supported
     */
    private String determineItemType(Element fragmentElement) {
        // Check for PhysicalInstance
        if (fragmentElement.getElementsByTagNameNS("ddi:physicalinstance:3_3", "PhysicalInstance").getLength() > 0) {
            return instanceConfiguration.itemTypes().get("PhysicalInstance");
        }
        // Check for DataRelationship
        if (fragmentElement.getElementsByTagNameNS("ddi:logicalproduct:3_3", "DataRelationship").getLength() > 0) {
            return instanceConfiguration.itemTypes().get("DataRelationship");
        }
        // Check for Variable
        if (fragmentElement.getElementsByTagNameNS("ddi:logicalproduct:3_3", "Variable").getLength() > 0) {
            return "683889c6-f74b-4d5e-92ed-908c0a42bb2d"; // Variable type UUID
        }
        // Check for CodeList
        if (fragmentElement.getElementsByTagNameNS("ddi:logicalproduct:3_3", "CodeList").getLength() > 0) {
            return "8b108ef8-b642-4484-9c49-f88e4bf7cf1d"; // CodeList type UUID
        }
        // Check for Category
        if (fragmentElement.getElementsByTagNameNS("ddi:logicalproduct:3_3", "Category").getLength() > 0) {
            return "7e47c269-bcab-40f7-a778-af7bbc4e3d00"; // Category type UUID
        }
        // Return null for unsupported types
        return null;
    }

    /**
     * Creates a secure DocumentBuilderFactory with XXE protection
     */
    private DocumentBuilderFactory createSecureDocumentBuilderFactory() throws Exception {
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
    private TransformerFactory createSecureTransformerFactory() throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");
        return factory;
    }

    /**
     * Convert a DOM Element to XML String
     */
    private String elementToString(Element element) throws Exception {
        TransformerFactory transformerFactory = createSecureTransformerFactory();
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

                String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
                String encodedAgencyId = URLEncoder.encode(agencyId, StandardCharsets.UTF_8);
                URL url = new URL(instanceConfiguration.baseApiUrl() + "ddiset/" + encodedAgencyId + "/" + encodedId);


                // Create headers with Bearer token
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(token);

                HttpEntity<Void> getRequestEntity = new HttpEntity<>(headers);

                // The response from Colectica ddiset endpoint contains XML with PhysicalInstance and DataRelationship
                String ddisetXml = restTemplate.exchange(
                        url.toString(),
                        HttpMethod.GET,
                        getRequestEntity,
                        String.class
                ).getBody();

                if (ddisetXml == null || ddisetXml.isEmpty()) {
                    return null;
                }


                // Clean the XML - remove all leading invisible/control characters until we hit '<'
                // This handles BOM, zero-width spaces, and other invisible characters
                int startIndex = 0;
                while (startIndex < ddisetXml.length() && ddisetXml.charAt(startIndex) != '<') {
                    char c = ddisetXml.charAt(startIndex);
                    if (c == '\uFEFF' || Character.isWhitespace(c) || Character.isISOControl(c) || !Character.isDefined(c)) {
                        startIndex++;
                    } else {
                        // Found a non-whitespace, non-control character that's not '<'
                        startIndex++;
                    }
                }

                if (startIndex > 0) {
                    ddisetXml = ddisetXml.substring(startIndex);
                }

                // Final trim for any trailing whitespace
                ddisetXml = ddisetXml.trim();

                logger.debug("Received DDI set XML from Colectica for Physical Instance and DataRelationship");

                // Parse the FragmentInstance XML and extract each Fragment
                // The ddiset endpoint returns a FragmentInstance containing multiple Fragment elements
                List<Ddi3Response.Ddi3Item> ddi3Items = parseFragmentInstanceToItems(ddisetXml, agencyId, id);

                Ddi3Response ddi3Response = new Ddi3Response(
                    null,  // options
                    ddi3Items
                );

                // Use the converter service to convert DDI3 to DDI4
                logger.info("Converting DDI3 to DDI4 using converter service");
                var response = ddi3ToDdi4Converter.convertDdi3ToDdi4(ddi3Response, "ddi:4.0");

                logger.info("Successfully converted Physical Instance to DDI4 format");
                return response;

            } catch (Exception e) {
                throw new RuntimeException("Failed to process DDI response", e);
            }
        });
    }

    @Override
    public Ddi4GroupResponse getGroup(String agencyId, String id) {
        logger.info("Fetching DDI4 Group from Colectica API for agencyId: {}, id: {}", agencyId, id);

        return authenticator.executeWithAuth(token -> {
            try {
                // Fetch the full DDI set (Group + StudyUnits) using the ddiset endpoint
                String ddisetUrl = instanceConfiguration.baseApiUrl() + "ddiset/"
                        + agencyId + "/"
                        + id;

                logger.info("Fetching full DDI set for Group from: {}", ddisetUrl);

                // Create headers with Bearer token
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(token);

                HttpEntity<Void> getRequestEntity = new HttpEntity<>(headers);

                // The response from Colectica ddiset endpoint contains XML with Group and StudyUnits
                String ddisetXml = restTemplate.exchange(
                        ddisetUrl,
                        HttpMethod.GET,
                        getRequestEntity,
                        String.class
                ).getBody();

                if (ddisetXml == null || ddisetXml.isEmpty()) {
                    logger.error("Received empty response from Colectica API for ddiset URL: {}", ddisetUrl);
                    return null;
                }

                logger.info("Received response from ddiset endpoint for Group. Length: {}", ddisetXml.length());

                // Clean the XML - remove leading invisible/control characters
                int startIndex = 0;
                while (startIndex < ddisetXml.length() && ddisetXml.charAt(startIndex) != '<') {
                    char c = ddisetXml.charAt(startIndex);
                    if (c == '\uFEFF' || Character.isWhitespace(c) || Character.isISOControl(c) || !Character.isDefined(c)) {
                        startIndex++;
                    } else {
                        logger.warn("Unexpected character at position {}: {} (code: {})", startIndex, c, (int)c);
                        startIndex++;
                    }
                }

                if (startIndex > 0) {
                    logger.info("Removed {} leading characters from XML", startIndex);
                    ddisetXml = ddisetXml.substring(startIndex);
                }

                ddisetXml = ddisetXml.trim();

                // Parse the XML directly to build Ddi4GroupResponse
                return parseGroupXmlToDdi4Response(ddisetXml);

            } catch (Exception e) {
                logger.error("Error processing Colectica API response for Group agencyId: {}, id: {}", agencyId, id, e);
                throw new RuntimeException("Failed to process DDI Group response", e);
            }
        });
    }

    /**
     * Parse DDI3 XML directly to build Ddi4GroupResponse
     */
    private Ddi4GroupResponse parseGroupXmlToDdi4Response(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        List<Ddi4Group> groups = new ArrayList<>();
        List<Ddi4StudyUnit> studyUnits = new ArrayList<>();
        List<TopLevelReference> topLevelReferences = new ArrayList<>();

        // Parse Group elements
        NodeList groupNodes = doc.getElementsByTagNameNS("ddi:group:3_3", "Group");
        logger.info("Found {} Group elements", groupNodes.getLength());

        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element groupElement = (Element) groupNodes.item(i);
            Ddi4Group group = parseGroupElement(groupElement);
            groups.add(group);

            // Add top level reference for the group
            topLevelReferences.add(new TopLevelReference(
                group.agency(),
                group.id(),
                group.version(),
                "Group"
            ));
        }

        // Parse StudyUnit elements
        NodeList studyUnitNodes = doc.getElementsByTagNameNS("ddi:studyunit:3_3", "StudyUnit");
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
        String isUniversallyUnique = groupElement.getAttribute("isUniversallyUnique");
        String versionDate = groupElement.getAttribute("versionDate");

        String urn = getElementTextContent(groupElement, "ddi:reusable:3_3", "URN");
        String agency = getElementTextContent(groupElement, "ddi:reusable:3_3", "Agency");
        String id = getElementTextContent(groupElement, "ddi:reusable:3_3", "ID");
        String version = getElementTextContent(groupElement, "ddi:reusable:3_3", "Version");
        String versionResponsibility = getElementTextContent(groupElement, "ddi:reusable:3_3", "VersionResponsibility");

        // Parse Citation
        Citation citation = parseCitation(groupElement);

        // Parse StudyUnitReferences
        List<StudyUnitReference> studyUnitReferences = parseStudyUnitReferences(groupElement);

        return new Ddi4Group(
            isUniversallyUnique.isEmpty() ? null : isUniversallyUnique,
            versionDate.isEmpty() ? null : versionDate,
            urn,
            agency,
            id,
            version,
            versionResponsibility,
            citation,
            studyUnitReferences
        );
    }

    /**
     * Parse a StudyUnit XML element to Ddi4StudyUnit
     */
    private Ddi4StudyUnit parseStudyUnitElement(Element studyUnitElement) {
        String isUniversallyUnique = studyUnitElement.getAttribute("isUniversallyUnique");
        String versionDate = studyUnitElement.getAttribute("versionDate");

        String urn = getElementTextContent(studyUnitElement, "ddi:reusable:3_3", "URN");
        String agency = getElementTextContent(studyUnitElement, "ddi:reusable:3_3", "Agency");
        String id = getElementTextContent(studyUnitElement, "ddi:reusable:3_3", "ID");
        String version = getElementTextContent(studyUnitElement, "ddi:reusable:3_3", "Version");

        // Parse Citation
        Citation citation = parseCitation(studyUnitElement);

        return new Ddi4StudyUnit(
            isUniversallyUnique.isEmpty() ? null : isUniversallyUnique,
            versionDate.isEmpty() ? null : versionDate,
            urn,
            agency,
            id,
            version,
            citation
        );
    }

    /**
     * Parse Citation from an element
     */
    private Citation parseCitation(Element parentElement) {
        NodeList citationNodes = parentElement.getElementsByTagNameNS("ddi:reusable:3_3", "Citation");
        if (citationNodes.getLength() == 0) {
            return null;
        }

        Element citationElement = (Element) citationNodes.item(0);
        NodeList titleNodes = citationElement.getElementsByTagNameNS("ddi:reusable:3_3", "Title");
        if (titleNodes.getLength() == 0) {
            return null;
        }

        Element titleElement = (Element) titleNodes.item(0);
        NodeList stringNodes = titleElement.getElementsByTagNameNS("ddi:reusable:3_3", "String");
        if (stringNodes.getLength() == 0) {
            return null;
        }

        Element stringElement = (Element) stringNodes.item(0);
        String text = stringElement.getTextContent();
        String lang = stringElement.getAttributeNS("http://www.w3.org/XML/1998/namespace", "lang");

        return new Citation(new Title(new StringValue(lang.isEmpty() ? "fr-FR" : lang, text)));
    }

    /**
     * Parse StudyUnitReferences from a Group element
     */
    private List<StudyUnitReference> parseStudyUnitReferences(Element groupElement) {
        List<StudyUnitReference> references = new ArrayList<>();

        NodeList refNodes = groupElement.getElementsByTagNameNS("ddi:reusable:3_3", "StudyUnitReference");
        for (int i = 0; i < refNodes.getLength(); i++) {
            Element refElement = (Element) refNodes.item(i);

            String agency = getElementTextContent(refElement, "ddi:reusable:3_3", "Agency");
            String id = getElementTextContent(refElement, "ddi:reusable:3_3", "ID");
            String version = getElementTextContent(refElement, "ddi:reusable:3_3", "Version");
            String typeOfObject = getElementTextContent(refElement, "ddi:reusable:3_3", "TypeOfObject");

            references.add(new StudyUnitReference(agency, id, version, typeOfObject));
        }

        return references;
    }

    /**
     * Get text content of a child element with given namespace and local name
     */
    private String getElementTextContent(Element parent, String namespaceUri, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespaceUri, localName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    @Override
    public void updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request) {
        // First, fetch the current instance to get all necessary information including variables
        Ddi4Response currentInstance = getPhysicalInstance(agencyId, id);

        if (currentInstance == null || currentInstance.physicalInstance() == null || currentInstance.physicalInstance().isEmpty()) {
            throw new RuntimeException("Physical instance not found: " + agencyId + "/" + id);
        }

        var currentPI = currentInstance.physicalInstance().get(0);
        var currentDR = currentInstance.dataRelationship() != null && !currentInstance.dataRelationship().isEmpty()
                ? currentInstance.dataRelationship().get(0)
                : null;

        // Get current timestamp in ISO format
        String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Determine new version (increment by 1)
        int currentVersion = Integer.parseInt(currentPI.version());
        String newVersion = String.valueOf(currentVersion + 1);

        // Build updated PhysicalInstance with new label if provided
        String newPhysicalInstanceLabel = request.physicalInstanceLabel() != null
                ? request.physicalInstanceLabel()
                : currentPI.citation().title().string().text();

        var updatedPI = new Ddi4PhysicalInstance(
                currentPI.isUniversallyUnique(),
                versionDate,
                currentPI.urn(),
                currentPI.agency(),
                currentPI.id(),
                newVersion,
                currentPI.basedOnObject(),
                new Citation(new Title(new StringValue(
                        currentPI.citation().title().string().xmlLang(),
                        newPhysicalInstanceLabel
                ))),
                currentPI.dataRelationshipReference()
        );

        // Build updated DataRelationship with new label if provided, preserving LogicalRecord with variables
        Ddi4DataRelationship updatedDR = null;
        if (currentDR != null) {
            // Build updated DataRelationship Label
            Label drLabel = createLabelWithFallback(currentDR.label(), request.dataRelationshipLabel());

            // Build updated LogicalRecord with new label if provided
            LogicalRecord updatedLR = currentDR.logicalRecord();
            if (updatedLR != null && request.logicalRecordLabel() != null) {
                updatedLR = new LogicalRecord(
                        updatedLR.isUniversallyUnique(),
                        updatedLR.urn(),
                        updatedLR.agency(),
                        updatedLR.id(),
                        updatedLR.version(),
                        updatedLR.logicalRecordName(),
                        createLabelWithFallback(updatedLR.label(), request.logicalRecordLabel()),
                        updatedLR.variablesInRecord()
                );
            }

            updatedDR = new Ddi4DataRelationship(
                    currentDR.isUniversallyUnique(),
                    versionDate,
                    currentDR.urn(),
                    currentDR.agency(),
                    currentDR.id(),
                    newVersion,
                    currentDR.basedOnObject(),
                    currentDR.dataRelationshipName(),
                    drLabel,
                    updatedLR // Updated LogicalRecord with new label
            );
        }

        // Build updated Ddi4Response preserving all variables, codeLists and categories
        Ddi4Response updatedResponse = new Ddi4Response(
                currentInstance.schema(),
                currentInstance.topLevelReference(),
                List.of(updatedPI),
                updatedDR != null ? List.of(updatedDR) : currentInstance.dataRelationship(),
                currentInstance.variable(),    // Preserve all variables
                currentInstance.codeList(),    // Preserve all codeLists
                currentInstance.category()     // Preserve all categories
        );

        // Use updateFullPhysicalInstance to save everything including variables
        updateFullPhysicalInstance(agencyId, id, updatedResponse);

    }

    @Override
    public void updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response) {
        logger.info("Updating full physical instance {}/{} with all DDI objects in Colectica", agencyId, id);

        authenticator.executeWithAuth(token -> {

            // Convert DDI4 to DDI3
            Ddi3Response ddi3Response = ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Response);

            if (ddi3Response == null || ddi3Response.items() == null || ddi3Response.items().isEmpty()) {
                throw new RuntimeException("No items to save in DDI4 response");
            }

            // Convert each Ddi3Item to ColecticaItemResponse
            List<ColecticaItemResponse> colecticaItems = ddi3Response.items().stream()
                .map(ddi3Item -> new ColecticaItemResponse(
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
                ))
                .toList();

            // Create request with all items
            ColecticaCreateItemRequest updateRequest = new ColecticaCreateItemRequest(colecticaItems);

            // Send to Colectica
            String url = instanceConfiguration.baseApiUrl() + "item";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<ColecticaCreateItemRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

            logger.info("Sending full update request to Colectica with {} items: {}", colecticaItems.size(), url);

            // POST to Colectica
            restTemplate.postForObject(url, requestEntity, String.class);

            logger.info("Successfully updated full physical instance with id: {} ({} items saved)", id, colecticaItems.size());


            return null;
        });
    }

    @Override
    public Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request) {
        return authenticator.executeWithAuth(token -> {
            // Generate UUIDs for physical instance and data relationship
            String physicalInstanceId = UUID.randomUUID().toString();
            String dataRelationshipId = UUID.randomUUID().toString();
            String logicalRecordId = UUID.randomUUID().toString();
            String agencyId = instanceConfiguration.defaultAgencyId();
            int version = 1;

            // Get current timestamp in ISO format
            String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

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
                    request.logicalRecordLabel() != null ? request.logicalRecordLabel() : request.physicalInstanceLabel(),
                    versionDate
            );

            // Create Colectica items
            ColecticaItemResponse physicalInstanceItem = new ColecticaItemResponse(
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
                    MUTUALIZED_CODE_LIST_UUID // DDI format UUID
            );

            ColecticaItemResponse dataRelationshipItem = new ColecticaItemResponse(
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
                    MUTUALIZED_CODE_LIST_UUID // DDI format UUID
            );

            // Create request with both items
            ColecticaCreateItemRequest createRequest = new ColecticaCreateItemRequest(
                    List.of(physicalInstanceItem, dataRelationshipItem)
            );

            // Send to Colectica
            String url = instanceConfiguration.baseApiUrl() + "item";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<ColecticaCreateItemRequest> requestEntity = new HttpEntity<>(createRequest, headers);

            // POST to Colectica
            restTemplate.postForObject(url, requestEntity, String.class);

            // Return the created instance
            return getPhysicalInstance(agencyId, physicalInstanceId);
        });
    }

    /**
     * Build DDI3 XML fragment for PhysicalInstance
     */
    private String buildPhysicalInstanceXml(String agencyId, String id, int version,
                                           String label, String dataRelationshipId, String versionDate) {
        return String.format("""
                <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                  <PhysicalInstance isUniversallyUnique="true" versionDate="%s" xmlns="ddi:physicalinstance:3_3">
                    <r:URN>urn:ddi:%s:%s:%d</r:URN>
                    <r:Agency>%s</r:Agency>
                    <r:ID>%s</r:ID>
                    <r:Version>%d</r:Version>
                    <r:Citation>
                      <r:Title>
                        <r:String xml:lang="fr-FR">%s</r:String>
                      </r:Title>
                    </r:Citation>
                    <r:DataRelationshipReference>
                      <r:Agency>%s</r:Agency>
                      <r:ID>%s</r:ID>
                      <r:Version>%d</r:Version>
                      <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                    </r:DataRelationshipReference>
                  </PhysicalInstance>
                </Fragment>""",
                escapeXml(versionDate), escapeXml(agencyId), escapeXml(id), version,
                escapeXml(agencyId), escapeXml(id), version, escapeXml(label),
                escapeXml(agencyId), escapeXml(dataRelationshipId), version
        );
    }

    /**
     * Build DDI3 XML fragment for DataRelationship
     */
    private String buildDataRelationshipXml(String agencyId, String dataRelationshipId, int version,
                                           String dataRelationshipLabel, String logicalRecordId,
                                           String logicalRecordLabel, String versionDate) {
        return String.format("""
                <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                  <DataRelationship isUniversallyUnique="true" versionDate="%s" xmlns="ddi:logicalproduct:3_3">
                    <r:URN>urn:ddi:%s:%s:%d</r:URN>
                    <r:Agency>%s</r:Agency>
                    <r:ID>%s</r:ID>
                    <r:Version>%d</r:Version>
                    <r:Label>
                      <r:Content xml:lang="fr-FR">%s</r:Content>
                    </r:Label>
                    <LogicalRecord isUniversallyUnique="true">
                      <r:URN>urn:ddi:%s:%s:%d</r:URN>
                      <r:Agency>%s</r:Agency>
                      <r:ID>%s</r:ID>
                      <r:Version>%d</r:Version>
                      <r:Label>
                        <r:Content xml:lang="fr-FR">%s</r:Content>
                      </r:Label>
                    </LogicalRecord>
                  </DataRelationship>
                </Fragment>""",
                escapeXml(versionDate), escapeXml(agencyId), escapeXml(dataRelationshipId), version,
                escapeXml(agencyId), escapeXml(dataRelationshipId), version, escapeXml(dataRelationshipLabel),
                escapeXml(agencyId), escapeXml(logicalRecordId), version,
                escapeXml(agencyId), escapeXml(logicalRecordId), version, escapeXml(logicalRecordLabel)
        );
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    @Override
    public List<PartialCodesList> getMutualizedCodesLists() {
        logger.info("Getting mutualized codes lists from Colectica API via _getDescriptions endpoint");

        List<MutualizedCodeListEntry> mutualizedEntries = colecticaConfiguration.mutualizedCodesLists();
        logger.info("Mutualized entries from config: {}", mutualizedEntries);

        if (mutualizedEntries == null || mutualizedEntries.isEmpty()) {
            logger.info("No mutualized codes lists configured");
            return List.of();
        }

        return authenticator.executeWithAuth(token -> {
            String url = instanceConfiguration.baseApiUrl() + "item/_getDescriptions";
            logger.info("Calling URL: {}", url);

            // Build request body from configuration
            List<GetDescriptionsRequest.IdentifierRef> identifiers = mutualizedEntries.stream()
                    .map(entry -> new GetDescriptionsRequest.IdentifierRef(
                            entry.agencyId(),
                            entry.identifier(),
                            entry.version()
                    ))
                    .toList();

            GetDescriptionsRequest requestBody = new GetDescriptionsRequest(identifiers);
            logger.info("Request body identifiers: {}", identifiers);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<GetDescriptionsRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            logger.info("Calling _getDescriptions with {} identifiers", identifiers.size());

            ColecticaItem[] response = restTemplate.postForObject(url, requestEntity, ColecticaItem[].class);
            logger.info("Response from _getDescriptions: {} items", response != null ? response.length : "null");

            if (response == null) {
                logger.warn("Received null response from _getDescriptions endpoint");
                return List.of();
            }

            return Arrays.stream(response)
                    .map(item -> {
                        String id = item.identifier();
                        String label = extractLabelFromItem(item);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = null;
                        try {
                            date = formatter.parse(item.versionDate());
                        } catch (ParseException | NullPointerException _) {
                            logger.debug("Impossible to parse {}", item.versionDate());
                        }
                        String agency = item.agencyId();
                        return new PartialCodesList(id, label, date, agency);
                    })
                    .toList();
        });
    }

    /**
     * Creates a Label with the given text, using the language from the existing label if available,
     * or falling back to the default language (fr-FR).
     *
     * @param existingLabel the existing label to extract language from (can be null)
     * @param newText the text for the new label
     * @return a new Label with the appropriate language, or null if newText is null
     */
    private Label createLabelWithFallback(Label existingLabel, String newText) {
        if (newText == null) {
            return existingLabel;
        }
        String lang = existingLabel != null && existingLabel.content() != null
                ? existingLabel.content().xmlLang()
                : DEFAULT_LANG;
        return new Label(new Content(lang, newText));
    }
}