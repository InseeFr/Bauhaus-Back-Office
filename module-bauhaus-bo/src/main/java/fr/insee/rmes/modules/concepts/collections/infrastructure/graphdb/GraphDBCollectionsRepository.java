package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.DiacriticSorter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GraphDBCollectionsRepository implements CollectionsRepository  {
    private final RepositoryGestion repositoryGestion;

    public GraphDBCollectionsRepository(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }

    @Override
    public List<PartialCollection> getCollections() throws CollectionsFetchException {


        try {
            var collections =  repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionsQuery());

            var response = DiacriticSorter.sort(collections,
                    GraphDBPartialCollection[].class,
                    GraphDBPartialCollection::label);

            return response.stream().map(GraphDBPartialCollection::toDomain).toList();
        } catch (Exception e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException {
        // TODO gerer le cas ou l'id n'existe pas en base
        try {
            var collection = repositoryGestion.getResponseAsObject(ConceptCollectionsQueries.collectionQuery(id.value()));

            if(collection.isEmpty()){
                return Optional.empty();
            }

            var graphDBCollection = Deserializer.deserializeJSONObject(collection, GraphDBCollection.class);

            return Optional.of(graphDBCollection.toDomain());

        } catch (RmesException e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public CollectionId save(CreateCollectionCommand collection) {
        return new CollectionId("1");
    }
}
