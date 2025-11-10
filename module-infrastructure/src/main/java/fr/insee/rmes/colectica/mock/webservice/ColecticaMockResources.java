package fr.insee.rmes.colectica.mock.webservice;

import fr.insee.rmes.colectica.dto.AuthenticationRequest;
import fr.insee.rmes.colectica.dto.AuthenticationResponse;
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

    @PostMapping("/api/_query")
    public ColecticaResponse getPhysicalInstances(@RequestBody QueryRequest queryRequest) {
        // For now, ignore the request body and return all instances
        // In a real implementation, we would filter based on itemTypes
        return mockDataService.getColecticaResponse();
    }

    @GetMapping("/physical-instances/{id}")
    public Map<String, String> getPhysicalInstance(@PathVariable String id) {
        return Map.of("id", id, "label", "Physical Instance " + id);
    }
}
