package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.port.serverside.ConceptsRepository;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.IntStream;

@ServerSideAdaptor
@Repository
public class GraphDBConceptsRepository implements ConceptsRepository {

    private final RepositoryGestion repositoryGestion;
    private final ConceptCollectionsQueries conceptCollectionsQueries;

    public GraphDBConceptsRepository(RepositoryGestion repositoryGestion, ConceptCollectionsQueries conceptCollectionsQueries) {
        this.repositoryGestion = repositoryGestion;
        this.conceptCollectionsQueries = conceptCollectionsQueries;
    }

    @Override
    public List<String> getCollectionIdsByConceptId(String conceptId) throws ConceptFetchException {
        try {
            JSONArray results = repositoryGestion.getResponseAsArray(conceptCollectionsQueries.getCollectionsByConceptId(conceptId));
            if (results == null) return List.of();
            return IntStream.range(0, results.length())
                    .mapToObj(i -> results.getJSONObject(i).getString("id"))
                    .toList();
        } catch (RmesException e) {
            throw new ConceptFetchException(e);
        }
    }
}
