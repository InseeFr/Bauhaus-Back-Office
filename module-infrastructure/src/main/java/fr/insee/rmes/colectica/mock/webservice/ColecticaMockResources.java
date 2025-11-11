package fr.insee.rmes.colectica.mock.webservice;

import fr.insee.rmes.colectica.dto.AuthenticationRequest;
import fr.insee.rmes.colectica.dto.AuthenticationResponse;
import fr.insee.rmes.colectica.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.dto.ColecticaResponse;
import fr.insee.rmes.colectica.dto.QueryRequest;
import fr.insee.rmes.colectica.mock.service.MockDataService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController()
@RequestMapping("/colectica")
@ConditionalOnProperty(name = "fr.insee.rmes.bauhaus.colectica.mock-server-enabled", havingValue = "true")
public class ColecticaMockResources {

    private final MockDataService mockDataService;

    public ColecticaMockResources(MockDataService mockDataService) {
        this.mockDataService = mockDataService;
    }

    @GetMapping
    public String getColectica() {
        return "Mock Colectica Server Response";
    }

    @PostMapping("/token/createtoken")
    public ResponseEntity<AuthenticationResponse> createToken(@RequestBody AuthenticationRequest authRequest) {
        // Mock implementation: Accept any credentials and return a mock token
        // In a real scenario, you would validate credentials here

        if (authRequest == null || authRequest.username() == null || authRequest.password() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // For mock purposes, reject empty credentials
        if (authRequest.username().isBlank() || authRequest.password().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Generate a mock token (in production, this would be a proper JWT or similar)
        String mockToken = "mock-token-" + UUID.randomUUID();

        return ResponseEntity.ok(new AuthenticationResponse(mockToken));
    }

    @PostMapping("/api/v1/_query")
    public ColecticaResponse getPhysicalInstances(@RequestBody QueryRequest queryRequest) {
        // For now, ignore the request body and return all instances
        // In a real implementation, we would filter based on itemTypes
        return mockDataService.getColecticaResponse();
    }


    @GetMapping("/api/v1/item/{agencyId}/{identifier}/{version}")
    public ResponseEntity<ColecticaItemResponse> getItem(
            @PathVariable String agencyId,
            @PathVariable String identifier,
            @PathVariable int version) {

        // Mock DDI XML content
        String ddiXml = """
                <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                  <PhysicalInstance isUniversallyUnique="true" versionDate="2025-10-23T12:28:43.615773Z" xmlns="ddi:physicalinstance:3_3">
                    <r:URN>urn:ddi:%s:%s:%d</r:URN>
                    <r:Agency>%s</r:Agency>
                    <r:ID>%s</r:ID>
                    <r:Version>%d</r:Version>
                    <r:Citation>
                      <r:Title>
                        <r:String xml:lang="fr-FR">Mock Physical Instance</r:String>
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
                agencyId, identifier, version,
                agencyId, UUID.randomUUID(), version
        );

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
}
