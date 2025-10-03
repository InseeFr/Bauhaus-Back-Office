package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.codeslist.CodesListRepository;
import fr.insee.rmes.domain.codeslist.model.CodesListDomain;
import fr.insee.rmes.graphdb.DefaultSortFieldResolver;
import fr.insee.rmes.graphdb.SparqlQueryBuilder;
import fr.insee.rmes.utils.DiacriticSorter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of CodesListRepository using SPARQL and RDF4J.
 */
@Repository
public class CodesListRepositoryImpl implements CodesListRepository {
    
    private final RepositoryGestion repositoryGestion;
    
    @Autowired
    public CodesListRepositoryImpl(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }
    
    @Override
    public List<CodesListDomain> findAllCodesLists(boolean partial, String properties) {
        try {
            JSONArray codeslists;
            Set<String> requestedProperties = parseProperties(properties);
            
            if (partial) {
                // Use PartialCodesList for partial code lists (skos:Collection)
                codeslists = repositoryGestion.getResponseAsArray(
                        SparqlQueryBuilder.forEntity(PartialCodesList.class)
                                .build()
                );
            } else {
                // Use CodesList for complete code lists (skos:ConceptScheme)
                SparqlQueryBuilder<CodesList> queryBuilder = SparqlQueryBuilder.forEntity(CodesList.class);
                Set<String> lazyLoadedFields = queryBuilder.getLazyLoadedFields();
                
                if (!requestedProperties.isEmpty()) {
                    // Sélection dynamique des propriétés spécifiées (exclure les champs lazy-loaded)
                    requestedProperties.stream()
                            .filter(property -> !lazyLoadedFields.contains(property))
                            .forEach(queryBuilder::select);
                } else {
                    // Si pas de propriétés spécifiées, prendre toutes sauf les champs lazy-loaded
                    String[] lazyFieldsArray = lazyLoadedFields.toArray(String[]::new);
                    queryBuilder.selectAllExcept(lazyFieldsArray);
                }
                
                codeslists = repositoryGestion.getResponseAsArray(queryBuilder.build());
            }

            // Convert infrastructure models to domain models using the converter
            List<CodesList> infrastructureModels = DiacriticSorter.sort(codeslists,
                    CodesList[].class,
                    DefaultSortFieldResolver.resolveSortFunction(CodesList.class));


            return infrastructureModels.stream()
                    .map(codesList -> {
                        CodesListDomain domain = CodesListConverter.toDomain(codesList);

                        if (shouldLoadLazyField(requestedProperties, "contributors")) {
                            List<String> contributors = findContributorsByCodesListUri(codesList.uri());
                            domain.setContributors(contributors);
                        }
                        
                        return domain;
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve codes lists", e);
        }
    }

    @Override
    public List<String> findContributorsByCodesListUri(String codesListUri) {
        try {
            JSONArray contributors = repositoryGestion.getResponseAsArray(
                    SparqlQueryBuilder.forEntity(CodesList.class)
                            .select("contributor")
                            .where("uri", codesListUri)
                            .build()
            );
            
            return contributors.toList().stream()
                    .map(obj -> extractStringValue(obj, "contributor"))
                    .filter(Objects::nonNull)
                    .filter(contributor -> !contributor.isEmpty())
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve contributors for codes list: " + codesListUri, e);
        }
    }
    
    private String extractStringValue(Object obj, String fieldName) {
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Object value = map.get(fieldName);
            return value instanceof String ? (String) value : null;
        }
        return null;
    }
    
    /**
     * Parses a comma-separated properties string into a Set.
     * 
     * @param properties comma-separated list of properties
     * @return Set of trimmed property names (empty set if null/empty input)
     */
    private Set<String> parseProperties(String properties) {
        if (properties == null || properties.trim().isEmpty()) {
            return Collections.emptySet();
        }
        
        return Arrays.stream(properties.split(","))
                .map(String::trim)
                .filter(property -> !property.isEmpty())
                .collect(Collectors.toSet());
    }
    
    /**
     * Determines if a lazy-loaded field should be loaded based on the requested properties.
     * 
     * @param requestedProperties Set of requested property names (empty means all properties)
     * @param fieldName the name of the lazy-loaded field to check
     * @return true if the field should be loaded
     */
    private boolean shouldLoadLazyField(Set<String> requestedProperties, String fieldName) {
        return requestedProperties.isEmpty() || requestedProperties.contains(fieldName);
    }
}