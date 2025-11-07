package fr.insee.rmes.modules.concepts.collection.infrastructure.graphdb;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collection.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collection.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collection.domain.model.PartialCollection;
import fr.insee.rmes.modules.concepts.collection.domain.port.serverside.CollectionsRepository;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.DiacriticSorter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
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
        IRI collectionURI = RdfUtils.collectionIRI(graphDBCollection.id());
        model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, RdfUtils.conceptGraph());
        model.add(collectionURI, INSEE.IS_VALIDATED, RdfUtils.setLiteralBoolean(graphDBCollection.isValidated()), RdfUtils.conceptGraph());
        model.add(collectionURI, DCTERMS.TITLE, RdfUtils.setLiteralString(graphDBCollection.prefLabelLg1(),
                graphDBCollection.lg1()), RdfUtils.conceptGraph());
        model.add(collectionURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(graphDBCollection.created()), RdfUtils.conceptGraph());
        model.add(collectionURI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(graphDBCollection.contributor()), RdfUtils.conceptGraph());
        model.add(collectionURI, DC.CREATOR, RdfUtils.setLiteralString(graphDBCollection.creator()), RdfUtils.conceptGraph());
        /*Optional*/
        RdfUtils.addTripleDateTime(collectionURI, DCTERMS.MODIFIED, graphDBCollection.modified(), model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.TITLE, graphDBCollection.prefLabelLg2(), graphDBCollection.lg2(), model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, graphDBCollection.descriptionLg1(), graphDBCollection.lg1(), model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, graphDBCollection.descriptionLg2(), graphDBCollection.lg2(), model, RdfUtils.conceptGraph());

        /*Members*/
        graphDBCollection.conceptIds().forEach(conceptId->{
            IRI memberIRI = RdfUtils.conceptIRI(conceptId);
            model.add(collectionURI, SKOS.MEMBER, memberIRI, RdfUtils.conceptGraph());
        });

        try {
            repositoryGestion.loadSimpleObject(collectionURI, model);
        } catch (RmesException e) {
            throw new CollectionsSaveException(e);
        }
    }
}
