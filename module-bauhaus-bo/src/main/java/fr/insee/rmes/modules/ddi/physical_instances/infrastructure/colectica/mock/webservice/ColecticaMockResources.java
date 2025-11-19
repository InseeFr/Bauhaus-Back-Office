package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.*;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock.service.MockDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mock REST controller that simulates Colectica API endpoints
 * using the secondary DDI repository instance
 */
@RestController()
@RequestMapping("/colectica")
@ConditionalOnProperty(name = "fr.insee.rmes.bauhaus.colectica.mock-server-enabled", havingValue = "true")
public class ColecticaMockResources {
    private static final Logger logger = LoggerFactory.getLogger(ColecticaMockResources.class);

    private final MockDataService mockDataService;
    private final ObjectMapper objectMapper;

    public ColecticaMockResources(MockDataService mockDataService, ObjectMapper objectMapper) {
        this.mockDataService = mockDataService;
        this.objectMapper = objectMapper;
        logger.info("Mock Colectica REST controller initialized using secondary DDI repository");
    }

    @GetMapping
    public String getColectica() {
        return "Mock Colectica Server - Using Secondary Instance";
    }

    /**
     * Mock authentication endpoint - validates credentials against secondary instance configuration
     */
    @PostMapping("/token/createtoken")
    public ResponseEntity<AuthenticationResponse> createToken(@RequestBody AuthenticationRequest authRequest) {
        if (authRequest == null || authRequest.username() == null || authRequest.password() == null) {
            logger.warn("Authentication failed: Missing credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // For mock purposes, accept any non-empty credentials
        if (authRequest.username().isBlank() || authRequest.password().isBlank()) {
            logger.warn("Authentication failed: Empty credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Generate a mock token
        String mockToken = "mock-token-secondary-" + UUID.randomUUID();

        logger.info("Authentication successful for secondary instance mock, token generated");
        return ResponseEntity.ok(new AuthenticationResponse(mockToken));
    }

    /**
     * Query physical instances - delegates to MockDataService (secondary repository)
     */
    @PostMapping("/api/v1/_query")
    public ColecticaResponse getPhysicalInstances(@RequestBody QueryRequest queryRequest) {
        logger.info("Mock secondary instance: Querying physical instances");

        // Get physical instances from secondary repository via MockDataService
        List<PartialPhysicalInstance> instances = mockDataService.getPhysicalInstances();

        // Convert PartialPhysicalInstance to ColecticaItem
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        List<ColecticaItem> colecticaItems = instances.stream()
                .map(instance -> {
                    String itemNameStr = instance.label() != null ? instance.label() : "Physical Instance";
                    String versionDate = instance.versionDate() != null ? formatter.format(instance.versionDate()) : null;

                    return new ColecticaItem(
                            Map.of(), // Summary
                            Map.of("en-US", itemNameStr), // ItemName
                            Map.of("en-US", itemNameStr), // Label
                            Map.of(), // Description
                            Map.of(), // VersionRationale
                            0, // MetadataRank
                            "", // RepositoryName
                            false, // IsAuthoritative
                            List.of(), // Tags
                            "a51e85bb-6259-4488-8df2-f08cb43485f8", // ItemType (PhysicalInstance type UUID)
                            instance.agency(), // AgencyId
                            1, // Version
                            instance.id(), // Identifier
                            null, // Item
                            "", // Notes
                            versionDate, // VersionDate
                            "mock.user@insee.fr", // VersionResponsibility
                            false, // IsPublished
                            false, // IsDeprecated
                            false, // IsProvisional
                            "dc337820-af3a-4c0b-82f9-cf02535cde83", // ItemFormat (DDI format UUID)
                            0L, // TransactionId
                            0 // VersionCreationType
                    );
                })
                .collect(Collectors.toList());

        logger.info("Returning {} physical instances from secondary repository", colecticaItems.size());
        return new ColecticaResponse(colecticaItems);
    }

    /**
     * Get specific item by agencyId and identifier (without version)
     * Delegates to MockDataService (secondary repository)
     */
    @GetMapping("/api/v1/item/{agencyId}/{identifier}")
    public ResponseEntity<ColecticaItemResponse> getItem(
            @PathVariable String agencyId,
            @PathVariable String identifier) {
        return getItemWithVersion(agencyId, identifier, 1);
    }

    /**
     * Get specific item by agencyId, identifier and version
     * Delegates to MockDataService (secondary repository)
     */
    @GetMapping("/api/v1/item/{agencyId}/{identifier}/{version}")
    public ResponseEntity<ColecticaItemResponse> getItemWithVersion(
            @PathVariable String agencyId,
            @PathVariable String identifier,
            @PathVariable int version) {

        logger.info("Mock secondary instance: Getting item {}/{}/{}", agencyId, identifier, version);

        // Get the physical instance from secondary repository
        Ddi4Response ddi4Response = mockDataService.getPhysicalInstanceById(agencyId, identifier);

        if (ddi4Response == null) {
            logger.warn("Physical instance not found with agencyId: {}, id: {}", agencyId, identifier);
            return ResponseEntity.notFound().build();
        }

        // Convert Ddi4Response to DDI XML format (simplified for mock)
        String ddiXml = convertDdi4ToXml(ddi4Response, agencyId, identifier, version);

        ColecticaItemResponse response = new ColecticaItemResponse(
                "a51e85bb-6259-4488-8df2-f08cb43485f8", // itemType (PhysicalInstance type UUID)
                agencyId,
                version,
                identifier,
                ddiXml,
                "2025-10-23T12:28:44.537174", // versionDate
                "mock.user@insee.fr", // versionResponsibility
                false, // isPublished
                false, // isDeprecated
                false, // isProvisional
                "dc337820-af3a-4c0b-82f9-cf02535cde83" // itemFormat (DDI format UUID)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get DDI set (FragmentInstance with PhysicalInstance and DataRelationship) by agencyId and identifier
     * Delegates to MockDataService which calls the secondary Colectica server's ddiset endpoint
     */
    @GetMapping(value = "/api/v1/ddiset/{agencyId}/{identifier}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getDdiSet(
            @PathVariable String agencyId,
            @PathVariable String identifier) {

        logger.info("Mock secondary instance: Getting DDI set for {}/{}", agencyId, identifier);

        // Get the DDI set from secondary repository via MockDataService
        // This will call the secondary Colectica server's /api/v1/ddiset endpoint
        Ddi4Response ddi4Response = mockDataService.getDdiSetFromSecondary(agencyId, identifier);

        if (ddi4Response == null) {
            logger.warn("DDI set not found with agencyId: {}, id: {}", agencyId, identifier);
            return ResponseEntity.notFound().build();
        }

        // Convert Ddi4Response to DDI set XML format (FragmentInstance with PhysicalInstance and DataRelationship)
        String ddisetXml = convertDdi4ToDdiSetXml(ddi4Response, agencyId, identifier, 1);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(ddisetXml);
    }


    /**
     * Convert DDI4 response to DDI3 XML format (simplified for mock)
     */
    private String convertDdi4ToXml(Ddi4Response ddi4Response, String agencyId, String identifier, int version) {
        return """
                <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                  <PhysicalInstance isUniversallyUnique="true" versionDate="2025-10-23T12:28:43.615773Z" xmlns="ddi:physicalinstance:3_3">
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
                </Fragment>""".formatted(
                agencyId, identifier, version,
                agencyId, identifier, version, ddi4Response.physicalInstance().get(0).citation().title().string().text(),
                agencyId, UUID.randomUUID(), version
        );
    }

    /**
     * Convert DDI4 response to DDI set XML (FragmentInstance with PhysicalInstance, DataRelationship, and Variables)
     */
    private String convertDdi4ToDdiSetXml(Ddi4Response ddi4Response, String agencyId, String identifier, int version) {
        String title = ddi4Response.physicalInstance() != null && !ddi4Response.physicalInstance().isEmpty()
                ? ddi4Response.physicalInstance().get(0).citation().title().string().text()
                : "Mock Physical Instance";

        String dataRelationshipId = ddi4Response.dataRelationship() != null && !ddi4Response.dataRelationship().isEmpty()
                ? ddi4Response.dataRelationship().get(0).id()
                : UUID.randomUUID().toString();

        String dataRelationshipName = ddi4Response.dataRelationship() != null && !ddi4Response.dataRelationship().isEmpty()
                ? ddi4Response.dataRelationship().get(0).dataRelationshipName().string().text()
                : "DataRelationshipName";

        return """
                <?xml version="1.0" encoding="utf-8"?>
                <ddi:FragmentInstance xmlns:r="ddi:reusable:3_3" xmlns:ddi="ddi:instance:3_3">
                    <ddi:TopLevelReference>
                        <r:Agency>%s</r:Agency>
                        <r:ID>%s</r:ID>
                        <r:Version>%d</r:Version>
                        <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
                    </ddi:TopLevelReference>
                    <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                        <PhysicalInstance isUniversallyUnique="true" versionDate="2025-10-23T12:28:43.615878Z" xmlns="ddi:physicalinstance:3_3">
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
                    </Fragment>
                    <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                        <DataRelationship isUniversallyUnique="true" versionDate="2025-10-23T12:28:43.608394Z" xmlns="ddi:logicalproduct:3_3">
                            <r:URN>urn:ddi:%s:%s:%d</r:URN>
                            <r:Agency>%s</r:Agency>
                            <r:ID>%s</r:ID>
                            <r:Version>%d</r:Version>
                            <DataRelationshipName>
                                <r:String xml:lang="en-US">%s</r:String>
                            </DataRelationshipName>
                            <LogicalRecord isUniversallyUnique="true">
                                <r:URN>urn:ddi:%s:logicalrecord-1:%d</r:URN>
                                <r:Agency>%s</r:Agency>
                                <r:ID>logicalrecord-1</r:ID>
                                <r:Version>%d</r:Version>
                                <LogicalRecordName>
                                    <r:String xml:lang="fr">%s</r:String>
                                </LogicalRecordName>
                            </LogicalRecord>
                        </DataRelationship>
                    </Fragment>
                </ddi:FragmentInstance>""".formatted(
                // TopLevelReference
                agencyId, identifier, version,
                // PhysicalInstance
                agencyId, identifier, version,
                agencyId, identifier, version, title,
                agencyId, dataRelationshipId, version,
                // DataRelationship
                agencyId, dataRelationshipId, version,
                agencyId, dataRelationshipId, version, dataRelationshipName,
                agencyId, version, agencyId, version, title
        );
    }
}
