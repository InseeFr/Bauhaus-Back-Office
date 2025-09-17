package fr.insee.rmes.colectica;

import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import fr.insee.rmes.domain.port.serverside.DDIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Repository
public class DDIRepositoryImpl implements DDIRepository {
    static final Logger logger = LoggerFactory.getLogger(DDIRepositoryImpl.class);

    private final RestTemplate restTemplate;
    private final ColecticaConfiguration colecticaConfiguration;

    public DDIRepositoryImpl(RestTemplate restTemplate, ColecticaConfiguration colecticaConfiguration) {
        this.restTemplate = restTemplate;
        this.colecticaConfiguration = colecticaConfiguration;
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info("Getting physical instances from Colectica mock via HTTP");
        
        String url = colecticaConfiguration.baseURI() + "/physical-instances";
        
        List<Map<String, String>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, String>>>() {}
        ).getBody();

        return response.stream()
                .map(item -> new PartialPhysicalInstance(item.get("id"), item.get("label")))
                .toList();
    }

    @Override
    public PhysicalInstance getPhysicalInstance(String id) {
        logger.info("Getting physical instance with id: {} from Colectica mock via HTTP", id);
        
        String url = colecticaConfiguration.baseURI() + "/physical-instances/" + id;
        
        Map<String, String> response = restTemplate.getForObject(url, Map.class);
        
        return new PhysicalInstance(response.get("id"), response.get("label"));
    }
}