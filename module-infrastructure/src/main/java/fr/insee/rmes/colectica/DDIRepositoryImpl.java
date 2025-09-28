package fr.insee.rmes.colectica;

import fr.insee.rmes.colectica.dto.ColecticaItem;
import fr.insee.rmes.colectica.dto.ColecticaResponse;
import fr.insee.rmes.colectica.dto.QueryRequest;
import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import fr.insee.rmes.domain.port.serverside.DDIRepository;
import jakarta.validation.constraints.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        
        String url = colecticaConfiguration.baseURI() + "/_query";
        
        // Create request body with itemTypes from configuration
        QueryRequest requestBody = new QueryRequest(colecticaConfiguration.itemTypes());
        
        ColecticaResponse response = restTemplate.postForObject(url, requestBody, ColecticaResponse.class);

        return response.results().stream()
                .map(item -> {
                    String id = item.identifier();
                    String label = extractLabelFromItem(item);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = null;
                    try {
                        date = formatter.parse(item.versionDate());
                    } catch (ParseException | NullPointerException e) {
                        logger.debug("Impossible to parse {}", item.versionDate());
                    }
                    return new PartialPhysicalInstance(id, label, date);
                })
                .toList();
    }
    
    private String extractLabelFromItem(ColecticaItem item) {
        // Extract label from ItemName or Label (both can have language variants)
        String label = extractLabelFromLanguageMap(item.itemName());
        if (label == null || label.trim().isEmpty()) {
            label = extractLabelFromLanguageMap(item.label());
        }
        if (label == null || label.trim().isEmpty()) {
            label = item.identifier(); // Fallback to ID if no label found
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

    @Override
    public PhysicalInstance getPhysicalInstance(String id) {
        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        String url = colecticaConfiguration.baseURI() + "/physical-instances/" + encodedId;

        Map<String, String> response = restTemplate.getForObject(url, Map.class);
        
        return new PhysicalInstance(response.get("id"), response.get("label"));
    }
}