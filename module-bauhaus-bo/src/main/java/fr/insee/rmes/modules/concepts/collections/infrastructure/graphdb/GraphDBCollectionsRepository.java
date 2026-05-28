package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.Language;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.model.concepts.CollectionForExportOld;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionExport;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionExportType;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XMLUtils;
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
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final CollectionsUtils collectionsUtils;
    private final CollectionExportBuilder collectionExportBuilder;
    private final ConceptsService conceptsService;
    private final int filenameMaxLength;

    public GraphDBCollectionsRepository(RepositoryGestion repositoryGestion,
                                        GraphDBCollectionProperties graphDBCollectionProperties,
                                        ConceptCollectionsQueries conceptCollectionsQueries,
                                        CollectionsUtils collectionsUtils,
                                        CollectionExportBuilder collectionExportBuilder,
                                        @Lazy ConceptsService conceptsService,
                                        @Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int filenameMaxLength) {
        this.repositoryGestion = repositoryGestion;
        this.graphDBCollectionProperties = graphDBCollectionProperties;
        this.conceptCollectionsQueries = conceptCollectionsQueries;
        this.collectionsUtils = collectionsUtils;
        this.collectionExportBuilder = collectionExportBuilder;
        this.conceptsService = conceptsService;
        this.filenameMaxLength = filenameMaxLength;
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
        model.add(collectionURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(graphDBCollection.validationState()), graph);
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

    @Override
    public void publishCollections(List<CollectionId> collectionIds) throws CollectionsSaveException {
        JSONArray ids = new JSONArray();
        collectionIds.forEach(id -> ids.put(id.value()));
        try {
            collectionsUtils.collectionsValidation(ids);
        } catch (RmesException e) {
            throw new CollectionsSaveException(e);
        }
    }

    @Override
    public CollectionExport exportCollection(CollectionId id) throws CollectionsFetchException {
        try {
            CollectionForExportOld collection = collectionExportBuilder.getCollectionDataOld(id.value());
            String collectionXml = XMLUtils.produceXMLResponse(collection).replace("CollectionForExport", "Collection");
            Map<String, String> xmlContent = new HashMap<>();
            xmlContent.put("collectionFile", collectionXml);
            String fileName = FilesUtils.generateFinalFileNameWithoutExtension(
                    collection.getId() + "-" + collection.getPrefLabelLg1(), filenameMaxLength);

            ResponseEntity<org.springframework.core.io.Resource> response =
                    collectionExportBuilder.exportAsResponse(fileName, xmlContent, true, true, true);
            org.springframework.core.io.Resource resource = response.getBody();
            if (resource == null) {
                throw new CollectionsFetchException(new RmesException(500, "Empty export resource", "ExportError"));
            }
            byte[] bytes = ((ByteArrayResource) resource).getByteArray();
            return new CollectionExport(fileName + FilesUtils.ODT_EXTENSION, bytes,
                    org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (RmesException e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public CollectionExport exportCollectionByType(CollectionId id, CollectionExportType type, Language language, boolean withConcepts) throws CollectionsFetchException {
        try {
            CollectionForExport collection = collectionExportBuilder.getCollectionData(id.value());
            List<String> conceptsIds = withConcepts ? memberConceptIds(id.value()) : List.of();
            Map<String, String> xmlContent = collectionXmlContent(collection);
            String fileName = exportFileName(collection, language);

            if (conceptsIds.isEmpty()) {
                ResponseEntity<org.springframework.core.io.Resource> response = (type == CollectionExportType.ODS)
                        ? collectionExportBuilder.exportAsResponseODS(fileName, xmlContent, true, true, true)
                        : collectionExportBuilder.exportAsResponseODT(fileName, xmlContent, true, language);
                byte[] bytes = ((ByteArrayResource) response.getBody()).getByteArray();
                String extension = (type == CollectionExportType.ODS) ? FilesUtils.ODS_EXTENSION : FilesUtils.ODT_EXTENSION;
                return new CollectionExport(fileName + extension, bytes, org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
            }

            Map<String, Map<String, String>> collections = new HashMap<>();
            collections.put(fileName, xmlContent);
            Map<String, Map<String, InputStream>> collectionConcepts = new HashMap<>();
            collectionConcepts.put(fileName, conceptsService.getConceptsExportIS(conceptsIds, null));

            byte[] bytes = (type == CollectionExportType.ODS)
                    ? collectionExportBuilder.buildOdsZipBytes(collections, true, true, true, collectionConcepts, withConcepts)
                    : collectionExportBuilder.buildOdtZipBytes(collections, true, true, true, language, collectionConcepts, withConcepts);
            return new CollectionExport(fileName + FilesUtils.ZIP_EXTENSION, bytes,
                    org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (RmesException e) {
            throw new CollectionsFetchException(e);
        }
    }

    @Override
    public CollectionExport exportCollectionsZip(List<CollectionId> ids, CollectionExportType type, Language language, boolean withConcepts) throws CollectionsFetchException {
        try {
            Map<String, Map<String, String>> collections = new HashMap<>();
            Map<String, Map<String, InputStream>> collectionsConcepts = new HashMap<>();

            for (CollectionId collectionId : ids) {
                try {
                    CollectionForExport collection = collectionExportBuilder.getCollectionData(collectionId.value());
                    List<String> conceptsIds = withConcepts ? memberConceptIds(collectionId.value()) : List.of();
                    Map<String, String> xmlContent = collectionXmlContent(collection);
                    String fileName = exportFileName(collection, language);
                    collections.put(fileName, xmlContent);
                    if (!conceptsIds.isEmpty()) {
                        collectionsConcepts.put(fileName, conceptsService.getConceptsExportIS(conceptsIds, null));
                    }
                } catch (RmesException ignored) {
                    // Mirror legacy behavior: skip individual failures, keep building the archive.
                }
            }

            String archiveName = collectionExportBuilder.computeZipFileName(collections);
            byte[] bytes = (type == CollectionExportType.ODS)
                    ? collectionExportBuilder.buildOdsZipBytes(collections, true, true, true, collectionsConcepts, withConcepts)
                    : collectionExportBuilder.buildOdtZipBytes(collections, true, true, true, language, collectionsConcepts, withConcepts);
            return new CollectionExport(archiveName, bytes, org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (RmesException e) {
            throw new CollectionsFetchException(e);
        }
    }

    private List<String> memberConceptIds(String collectionId) throws RmesException {
        var concepts = repositoryGestion.getResponseAsArray(conceptCollectionsQueries.collectionMembersQuery(collectionId));
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < concepts.length(); i++) {
            ids.add(concepts.getJSONObject(i).getString("id"));
        }
        return ids;
    }

    private Map<String, String> collectionXmlContent(CollectionForExport collection) {
        Map<String, String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile", XMLUtils.produceXMLResponse(collection).replace("CollectionForExport", "Collection"));
        return xmlContent;
    }

    private String exportFileName(CollectionForExport collection, Language language) {
        String label = (language == Language.lg2 && collection.getPrefLabelLg2() != null)
                ? collection.getPrefLabelLg2() : collection.getPrefLabelLg1();
        return FilesUtils.generateFinalFileNameWithoutExtension(collection.getId() + "-" + label, filenameMaxLength);
    }
}
