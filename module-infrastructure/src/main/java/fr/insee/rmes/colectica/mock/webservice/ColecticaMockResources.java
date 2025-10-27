package fr.insee.rmes.colectica.mock.webservice;

import fr.insee.rmes.colectica.dto.ColecticaResponse;
import fr.insee.rmes.colectica.dto.QueryRequest;
import fr.insee.rmes.colectica.mock.service.MockDataService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @PostMapping("/_query")
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
