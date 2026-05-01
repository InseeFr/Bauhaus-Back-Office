package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ServerSideAdaptor
@Repository
public class GraphDBCollectionsRepository implements CollectionsRepository  {
    static ValueFactory factory =  SimpleValueFactory.getInstance();

    private final RepositoryGestion repositoryGestion;
    private final GraphDBCollectionProperties graphDBCollectionProperties;
    private final ConceptCollectionsQueries conceptCollectionsQueries;

    public GraphDBCollectionsRepository(RepositoryGestion repositoryGestion, GraphDBCollectionProperties graphDBCollectionProperties, ConceptCollectionsQueries conceptCollectionsQueries) {
        this.repositoryGestion = repositoryGestion;
        this.graphDBCollectionProperties = graphDBCollectionProperties;
        this.conceptCollectionsQueries = conceptCollectionsQueries;
    }

    @Override
    public List<CompactCollection> getCollections() throws CollectionsFetchException {


        try {
            var collections =  repositoryGestion.getResponseAsArray(conceptCollectionsQueries.collectionsQuery());

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
            var collection = repositoryGestion.getResponseAsObject(conceptCollectionsQueries.collectionQuery(id.value().toString()));

            if(collection.isEmpty()){
                return Optional.empty();
            }

            var graphDBCollection = Deserializer.deserializeJSONObject(collection, GraphDBCollection.class);

            var concepts = repositoryGestion.getResponseAsArray(conceptCollectionsQueries.collectionMembersQuery(id.value().toString()));
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

        RdfUtils.addTripleUri(collectionURI, DC.CONTRIBUTOR, graphDBCollection.contributor(), model, graph);
        RdfUtils.addTripleUri(collectionURI, DC.CREATOR, graphDBCollection.creator(), model, graph);

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

    @Override
    public void update(Collection collection) throws CollectionsSaveException {
        this.save(collection);
    }

    @Override
    public List<CollectionDashboardItem> getDashboard() throws CollectionsFetchException {
        try {
            var results = repositoryGestion.getResponseAsArray(conceptCollectionsQueries.collectionsDashboardQuery());
            return Arrays.stream(Deserializer.deserializeJSONArray(results, GraphDBCollectionDashboardItem[].class))
                    .map(GraphDBCollectionDashboardItem::toDomain).toList();
        } catch (Exception e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public List<CollectionToValidate> getToValidate() throws CollectionsFetchException {
        try {
            var results = repositoryGestion.getResponseAsArray(conceptCollectionsQueries.collectionsToValidateQuery());
            return Arrays.stream(Deserializer.deserializeJSONArray(results, GraphDBCollectionToValidate[].class))
                    .map(GraphDBCollectionToValidate::toDomain).toList();
        } catch (Exception e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public List<CollectionMember> getCollectionMembers(CollectionId id) throws CollectionsFetchException {
        try {
            var results = repositoryGestion.getResponseAsArray(conceptCollectionsQueries.collectionMembersQuery(id.value().toString()));
            return Arrays.stream(Deserializer.deserializeJSONArray(results, GraphDBConcept[].class))
                    .map(GraphDBConcept::toDomain).toList();
        } catch (Exception e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public Set<String> findExistingCollectionIds(List<String> ids) throws CollectionsFetchException {
        if (ids.isEmpty()) return Set.of();
        try {
            var results = repositoryGestion.getResponseAsArray(conceptCollectionsQueries.findExistingCollectionIds(ids));
            if (results == null) return Set.of();
            return IntStream.range(0, results.length())
                    .mapToObj(i -> results.getJSONObject(i).getString("id"))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public List<String> getCollectionIdsByConceptId(String conceptId) throws CollectionsFetchException {
        try {
            var results = repositoryGestion.getResponseAsArray(conceptCollectionsQueries.getCollectionsByConceptId(conceptId));
            if (results == null) return List.of();
            return IntStream.range(0, results.length())
                    .mapToObj(i -> results.getJSONObject(i).getString("id"))
                    .toList();
        } catch (Exception e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public void linkConceptToCollection(CollectionId collectionId, String conceptId) throws CollectionsSaveException {
        try {
            String conceptUri = RdfUtils.conceptIRI(conceptId).toString();
            String graph = graphDBCollectionProperties.getResourceGraph().toString();
            repositoryGestion.executeUpdate(conceptCollectionsQueries.linkConceptToCollection(collectionId.value(), conceptUri, graph));
        } catch (RmesException e) {
            throw new CollectionsSaveException(e);
        }
    }

    @Override
    public void unlinkConceptFromCollection(CollectionId collectionId, String conceptId) throws CollectionsSaveException {
        try {
            String conceptUri = RdfUtils.conceptIRI(conceptId).toString();
            String graph = graphDBCollectionProperties.getResourceGraph().toString();
            repositoryGestion.executeUpdate(conceptCollectionsQueries.unlinkConceptFromCollection(collectionId.value(), conceptUri, graph));
        } catch (RmesException e) {
            throw new CollectionsSaveException(e);
        }
    }
}
