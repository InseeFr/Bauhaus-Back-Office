package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.Language;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.model.concepts.PartialCollection;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XMLUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ConceptsCollectionServiceImpl extends RdfService implements ConceptsCollectionService {
    static final Logger logger = LoggerFactory.getLogger(ConceptsCollectionServiceImpl.class);

    int maxLength;
    private final CollectionExportBuilder collectionExport;
    private final ConceptsService conceptsService;

    public ConceptsCollectionServiceImpl(
            CollectionExportBuilder collectionExport,
            ConceptsService conceptsService,
            @Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength) {
        this.collectionExport = collectionExport;
        this.conceptsService = conceptsService;
        this.maxLength = maxLength;
    }


    @Override
    public List<PartialCollection> getCollections()  throws RmesException{
        var collections =  repoGestion.getResponseAsArray(CollectionsQueries.collectionsQuery());

        return DiacriticSorter.sort(collections,
                PartialCollection[].class,
                PartialCollection::label);
    }

    @Override
    public String getCollectionsDashboard()  throws RmesException{
        return repoGestion.getResponseAsArray(CollectionsQueries.collectionsDashboardQuery()).toString();
    }

    @Override
    public String getCollectionByID(String id)  throws RmesException{
        return repoGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id)).toString();
    }

    @Override
    public String getCollectionMembersByID(String id)  throws RmesException{
        return repoGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(id)).toString();
    }

    private Map<String, String> convertCollectionInXml(CollectionForExport collection) {
        String collectionXml = XMLUtils.produceXMLResponse(collection);
        Map<String,String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile",  collectionXml.replace("CollectionForExport", "Collection"));
        return xmlContent;
    }

    private List<String> getCollectionConceptsIds(String collectionId) throws RmesException {
        List<String> conceptsIds = new ArrayList<>();
        JSONArray concepts = repoGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(collectionId));
        for(int i = 0; i < concepts.length(); i++){
            conceptsIds.add(concepts.getJSONObject(i).getString("id"));
        }
        return conceptsIds;
    }

    @Override
    public ResponseEntity<?> getCollectionExportODT(String id, String accept, Language lg, boolean withConcepts, HttpServletResponse response) throws RmesException {
        logger.info("Exporting a collection {} to odt", id);

        try {
            CollectionForExport collection = collectionExport.getCollectionData(id);
            List<String> conceptsIds = withConcepts ? getCollectionConceptsIds(id) : Collections.emptyList();
            Map<String, String> xmlContent = convertCollectionInXml(collection);

            String fileName = getFileNameForExport(collection, lg);
            if(conceptsIds.isEmpty()){
                return collectionExport.exportAsResponseODT(fileName, xmlContent,true, lg);
            }

            Map<String, InputStream> concepts = conceptsService.getConceptsExportIS(conceptsIds, null);
            Map<String, Map<String, String>> collections = new HashMap<>();
            collections.put(fileName, xmlContent);

            Map<String, Map<String, InputStream>> collectionConcepts = new HashMap<>();
            collectionConcepts.put(fileName, concepts);
            collectionExport.exportMultipleCollectionsAsZipOdt(collections, true, true, true, response, lg, collectionConcepts, withConcepts);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e){
            return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
        }
    }

    @Override
    public ResponseEntity<?> getCollectionExportODS(String id, String accept, boolean withConcepts, HttpServletResponse response) {
        logger.info("Exporting a collection {} to ods", id);

        try {
            CollectionForExport collection = collectionExport.getCollectionData(id);
            List<String>  conceptsIds = withConcepts ? getCollectionConceptsIds(id) : Collections.emptyList();
            Map<String, String> xmlContent = convertCollectionInXml(collection);

            String fileName = getFileNameForExport(collection, null);

            if(conceptsIds.isEmpty()){
                return collectionExport.exportAsResponseODS(fileName,xmlContent,true,true,true);
            }
            Map<String, InputStream> concepts = conceptsService.getConceptsExportIS(conceptsIds, null);
            Map<String, Map<String, String>> collections = new HashMap<>();
            collections.put(fileName, xmlContent);
            Map<String, Map<String, InputStream>> collectionConcepts = new HashMap<>();
            collectionConcepts.put(fileName, concepts);

            collectionExport.exportMultipleCollectionsAsZipOds(collections, true, true, true, response, collectionConcepts, withConcepts);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e){
            return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
        }
    }


    @Override
    public void exportZipCollection(String ids, String acceptHeader, HttpServletResponse response, Language lg, String type, boolean withConcepts) throws RmesException {
        Map<String, Map<String, String>> collections = new HashMap<>();
        Map<String, Map<String, InputStream>> collectionsConcepts = new HashMap<>();

        Arrays.asList(ids.split("_AND_")).forEach(id -> {
            try {
                List<String>  conceptsIds = withConcepts ? getCollectionConceptsIds(id) : Collections.emptyList();

                CollectionForExport collection = collectionExport.getCollectionData(id);
                Map<String, String> xmlContent = convertCollectionInXml(collection);

                String fileName = getFileNameForExport(collection, lg);
                collections.put(fileName, xmlContent);

                if(!conceptsIds.isEmpty()){
                    Map<String, InputStream> concepts = conceptsService.getConceptsExportIS(conceptsIds, null);
                    collectionsConcepts.put(fileName, concepts);
                }
            } catch (RmesException e) {
                logger.error(e.getMessage());
                logger.error(e.getDetails());
            }
        });

        if("odt".equalsIgnoreCase(type)){
            collectionExport.exportMultipleCollectionsAsZipOdt(collections, true, true, true, response, lg, collectionsConcepts, withConcepts);

        } else {
            collectionExport.exportMultipleCollectionsAsZipOds(collections, true, true, true, response, collectionsConcepts, withConcepts);
        }
    }

    private String getFileNameForExport(CollectionForExport collection, Language lg){
        String label = (lg == Language.lg2 && collection.getPrefLabelLg2() != null) ? collection.getPrefLabelLg2() : collection.getPrefLabelLg1();
        return FilesUtils.generateFinalFileNameWithoutExtension(collection.getId() + "-" + label, maxLength);
    }

}