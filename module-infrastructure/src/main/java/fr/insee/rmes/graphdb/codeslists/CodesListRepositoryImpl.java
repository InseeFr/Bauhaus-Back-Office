package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.domain.codeslist.CodesListRepository;
import fr.insee.rmes.domain.codeslist.model.CodesListDomain;
import fr.insee.rmes.graphdb.DefaultSortFieldResolver;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.sparql.SparqlQueryBuilder;
import fr.insee.rmes.utils.DiacriticSorter;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

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
    public List<CodesListDomain> findAllCodesLists(boolean partial) {
        try {
            JSONArray codeslists;

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

                // Si pas de propriétés spécifiées, prendre toutes sauf les champs lazy-loaded
                String[] lazyFieldsArray = lazyLoadedFields.toArray(String[]::new);
                queryBuilder.selectAllExcept(lazyFieldsArray);
                
                codeslists = repositoryGestion.getResponseAsArray(queryBuilder.build());
            }

            // Convert infrastructure models to domain models using the converter
            List<CodesList> infrastructureModels = DiacriticSorter.sort(codeslists,
                    CodesList[].class,
                    DefaultSortFieldResolver.resolveSortFunction(CodesList.class));


            return infrastructureModels.stream()
                    .map(codesList -> {
                        CodesListDomain domain = CodesListConverter.toDomain(codesList);
                        return domain;
                    })
                    .toList();
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve codes lists", e);
        }
    }

}