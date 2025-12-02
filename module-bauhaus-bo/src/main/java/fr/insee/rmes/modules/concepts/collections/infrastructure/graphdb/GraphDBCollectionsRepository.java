package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.DiacriticSorter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@ServerSideAdaptor
@Repository
public class GraphDBCollectionsRepository implements CollectionsRepository  {
    static ValueFactory factory =  SimpleValueFactory.getInstance();

    private final RepositoryGestion repositoryGestion;
    private final GraphDBCollectionProperties graphDBCollectionProperties;

    public GraphDBCollectionsRepository(RepositoryGestion repositoryGestion, GraphDBCollectionProperties graphDBCollectionProperties) {
        this.repositoryGestion = repositoryGestion;
        this.graphDBCollectionProperties = graphDBCollectionProperties;
    }

    @Override
    public List<CompactCollection> getCollections() throws CollectionsFetchException {


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
        try {
            var collection = repositoryGestion.getResponseAsObject(ConceptCollectionsQueries.collectionQuery(id.value().toString()));

            if(collection.isEmpty()){
                return Optional.empty();
            }

            var graphDBCollection = Deserializer.deserializeJSONObject(collection, GraphDBCollection.class);

            var concepts = repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionMembersQuery(id.value().toString()));
            var graphDBCollectionWithConcepts = graphDBCollection.withConcepts(Deserializer.deserializeJSONArray(concepts, GraphDBConcept[].class));

            return Optional.of(graphDBCollectionWithConcepts.toDomain());

        } catch (RmesException e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public void save(Collection collection) throws CollectionsSaveException {

        GraphDBCollection graphDBCollection = GraphDBCollection.fromDomain(collection);
        Model model = new LinkedHashModel();

        IRI collectionURI = graphDBCollectionProperties.getResourceIRI(collection.id().value());
        Resource graph = graphDBCollectionProperties.getResourceGraph();

        model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, graph);
        model.add(collectionURI, INSEE.IS_VALIDATED, RdfUtils.setLiteralBoolean(graphDBCollection.isValidated()), graph);
        model.add(collectionURI, DCTERMS.TITLE, RdfUtils.setLiteralString(graphDBCollection.prefLabelLg1(),
                graphDBCollection.prefLabelLg1_lg()), graph);
        model.add(collectionURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(graphDBCollection.created()), graph);
        model.add(collectionURI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(graphDBCollection.contributor()), graph);
        model.add(collectionURI, DC.CREATOR, RdfUtils.setLiteralString(graphDBCollection.creator()), graph);
        /*Optional*/
        RdfUtils.addTripleDateTime(collectionURI, DCTERMS.MODIFIED, graphDBCollection.modified(), model, graph);
        RdfUtils.addTripleString(collectionURI, DCTERMS.TITLE, graphDBCollection.prefLabelLg2(), graphDBCollection.prefLabelLg2_lg(), model, graph);
        RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, graphDBCollection.descriptionLg1(), graphDBCollection.descriptionLg1_lg(), model, graph);
        RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, graphDBCollection.descriptionLg2(), graphDBCollection.descriptionLg2_lg(), model, graph);

        /*Members*/
        graphDBCollection.conceptIds().forEach(conceptId->{
            IRI memberIRI = RdfUtils.conceptIRI(conceptId);
            model.add(collectionURI, SKOS.MEMBER, memberIRI, graph);
        });

        try {
            repositoryGestion.loadSimpleObject(collectionURI, model);
        } catch (RmesException e) {
            throw new CollectionsSaveException(e);
        }
    }
}
