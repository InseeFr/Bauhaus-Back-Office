package fr.insee.rmes.colectica.mock.webservice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/colectica")
@ConditionalOnProperty(name = "fr.insee.rmes.bauhaus.colectica.mock-server-enabled", havingValue = "true")
public class ColecticaMockResources {

    @GetMapping
    public String getColectica() {
        return "Mock Colectica Server Response";
    }

    @GetMapping("/physical-instances")
    public List<Map<String, String>> getPhysicalInstances() {
        return List.of(
                Map.of("id", "pi-1", "label", "Physical Instance 1"),
                Map.of("id", "pi-2", "label", "Physical Instance 2"),
                Map.of("id", "pi-3", "label", "Physical Instance 3")
        );
    }

    @GetMapping("/physical-instances/{id}")
    public Map<String, String> getPhysicalInstance(@PathVariable String id) {
        return Map.of("id", id, "label", "Physical Instance " + id);
    }
}
