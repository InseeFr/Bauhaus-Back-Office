package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaItem;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaResponse;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.QueryRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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
    private final ObjectMapper objectMapper;
    private Ddi4Response cachedDdi4Response;

    public DDIRepositoryImpl(
            RestTemplate restTemplate,
            ColecticaConfiguration colecticaConfiguration,
            ObjectMapper objectMapper
            ) {
        this.restTemplate = restTemplate;
        this.colecticaConfiguration = colecticaConfiguration;
        this.objectMapper = objectMapper;
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
                    } catch (ParseException | NullPointerException _) {
                        logger.debug("Impossible to parse {}", item.versionDate());
                    }
                    return new PartialPhysicalInstance(id, label, date);
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
    public Ddi4Response getPhysicalInstance(String id) {
        if (cachedDdi4Response != null) {
            logger.info("Returning cached DDI4 Physical Instance");
            return cachedDdi4Response;
        }

        logger.info("Loading DDI4 Physical Instance from static JSON file");

        Ddi4Response response = null;
        try {
            ClassPathResource resource = new ClassPathResource("sample-ddi4-data.json");
            String jsonResponse = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Remove BOM if present
            if (jsonResponse.startsWith("\uFEFF")) {
                jsonResponse = jsonResponse.substring(1);
            }

            response = objectMapper.readValue(jsonResponse, Ddi4Response.class);
        } catch (IOException e) {
            logger.error("Error loading or parsing JSON from static file", e);
            return null;
        }

        if (response != null) {
            // Keep only the first Physical Instance and DataRelationship
            var physicalInstances = response.physicalInstance();
            var dataRelationships = response.dataRelationship();

            cachedDdi4Response = new Ddi4Response(
                    response.schema(),
                    response.topLevelReference(),
                    physicalInstances != null && !physicalInstances.isEmpty() ?
                            List.of(physicalInstances.get(0)) : List.of(),
                    dataRelationships != null && !dataRelationships.isEmpty() ?
                            List.of(dataRelationships.get(0)) : List.of(),
                    response.variable(),
                    response.codeList(),
                    response.category()
            );
        }

        return cachedDdi4Response;
    }

    @Override
    public void updatePhysicalInstance(String id, UpdatePhysicalInstanceRequest request) {
        if (cachedDdi4Response != null) {
            logger.info("Updating cached DDI4 Physical Instance data");
            
            var physicalInstances = cachedDdi4Response.physicalInstance();
            var dataRelationships = cachedDdi4Response.dataRelationship();
            
            // Update Physical Instance value if present
            if (physicalInstances != null && !physicalInstances.isEmpty() && request.physicalInstanceLabel() != null) {
                var currentPI = physicalInstances.get(0);
                var updatedTitle = new Title(
                    new StringValue(
                        currentPI.citation().title().string().xmlLang(),
                        request.physicalInstanceLabel()
                    )
                );
                var updatedCitation = new Citation(updatedTitle);
                var updatedPI = new Ddi4PhysicalInstance(
                    currentPI.isUniversallyUnique(),
                    currentPI.versionDate(),
                    currentPI.urn(),
                    currentPI.agency(),
                    currentPI.id(),
                    currentPI.version(),
                    updatedCitation,
                    currentPI.dataRelationshipReference()
                );
                physicalInstances = List.of(updatedPI);
            }
            
            // Update DataRelationship name if present
            if (dataRelationships != null && !dataRelationships.isEmpty() && request.dataRelationshipName() != null) {
                var currentDR = dataRelationships.get(0);
                var updatedName = new DataRelationshipName(
                    new StringValue(
                        currentDR.dataRelationshipName().string().xmlLang(),
                        request.dataRelationshipName()
                    )
                );
                var updatedDR = new Ddi4DataRelationship(
                    currentDR.isUniversallyUnique(),
                    currentDR.versionDate(),
                    currentDR.urn(),
                    currentDR.agency(),
                    currentDR.id(),
                    currentDR.version(),
                    updatedName,
                    currentDR.logicalRecord()
                );
                dataRelationships = List.of(updatedDR);
            }
            
            // Update cached response
            cachedDdi4Response = new Ddi4Response(
                cachedDdi4Response.schema(),
                cachedDdi4Response.topLevelReference(),
                physicalInstances,
                dataRelationships,
                cachedDdi4Response.variable(),
                cachedDdi4Response.codeList(),
                cachedDdi4Response.category()
            );
        }
    }
}