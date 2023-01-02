package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.ConceptsCollectionsResources;
import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

@Service
public class ConceptsCollectionServiceImpl extends RdfService implements ConceptsCollectionService {
    static final Logger logger = LogManager.getLogger(ConceptsCollectionServiceImpl.class);

    @Autowired
    CollectionExportBuilder collectionExport;

    @Autowired
    ConceptsService conceptsService;

    @Override
    public String getCollections()  throws RmesException{
        return repoGestion.getResponseAsArray(CollectionsQueries.collectionsQuery()).toString();
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
        List conceptsIds = new ArrayList<String>();
        JSONArray concepts = repoGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(collectionId));
        for(int i = 0; i < concepts.length(); i++){
            conceptsIds.add(concepts.getJSONObject(i).getString("id"));
        }
        return conceptsIds;
    }

    @Override
    public ResponseEntity<?> getCollectionExportODT(String id, String accept, ConceptsCollectionsResources.Language lg, boolean withConcepts, HttpServletResponse response) throws RmesException {
        logger.info("Exporting a collection {} to odt", id);

        try {
            CollectionForExport collection = collectionExport.getCollectionData(id);
            List conceptsIds = withConcepts ? getCollectionConceptsIds(id) : Collections.emptyList();
            Map<String, String> xmlContent = convertCollectionInXml(collection);
            String fileName = getFileNameForExport(collection, lg);
            if(conceptsIds.size() == 0){
                return collectionExport.exportAsResponseODT(fileName,xmlContent,true,true,true, lg);
            }

            Map<String, InputStream> concepts = conceptsService.getConceptsExportIS(conceptsIds);
            Map<String, Map<String, String>> collections = new HashMap<>();
            collections.put(fileName, xmlContent);

            Map<String, Map<String, InputStream>> collectionConcepts = new HashMap<>();
            collectionConcepts.put(fileName, concepts);
            collectionExport.exportMultipleCollectionsAsZipOdt(collections, true, true, true, response, lg, collectionConcepts);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e){
            return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
        }
    }

    @Override
    public ResponseEntity<?> getCollectionExportODS(String id, String accept, boolean withConcepts, HttpServletResponse response) throws RmesException {
        logger.info("Exporting a collection {} to ods", id);

        try {
            CollectionForExport collection = collectionExport.getCollectionData(id);
            List conceptsIds = withConcepts ? getCollectionConceptsIds(id) : Collections.emptyList();
            Map<String, String> xmlContent = convertCollectionInXml(collection);
            String fileName = getFileNameForExport(collection, null);
            if(conceptsIds.size() == 0){
                return collectionExport.exportAsResponseODS(fileName,xmlContent,true,true,true);
            }
            Map<String, InputStream> concepts = conceptsService.getConceptsExportIS(conceptsIds);
            Map<String, Map<String, String>> collections = new HashMap<>();
            collections.put(fileName, xmlContent);
            Map<String, Map<String, InputStream>> collectionConcepts = new HashMap<>();
            collectionConcepts.put(fileName, concepts);

            collectionExport.exportMultipleCollectionsAsZipOds(collections, true, true, true, response, collectionConcepts);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e){
            return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
        }
    }

    @Override
    public void exportZipCollection(String ids, String acceptHeader, HttpServletResponse response, ConceptsCollectionsResources.Language lg, String type, boolean withConcepts) throws RmesException {

        Map<String, Map<String, String>> collections = new HashMap<>();
        Map<String, Map<String, InputStream>> collectionsConcepts = new HashMap<>();

        Arrays.asList(ids.split(",")).forEach(id -> {
            try {
                List conceptsIds = withConcepts ? getCollectionConceptsIds(id) : Collections.emptyList();

                CollectionForExport collection = collectionExport.getCollectionData(id);
                Map<String, String> xmlContent = convertCollectionInXml(collection);
                String fileName = getFileNameForExport(collection, lg);
                collections.put(fileName, xmlContent);

                if(conceptsIds.size() > 0){
                    Map<String, InputStream> concepts = conceptsService.getConceptsExportIS(conceptsIds);
                    collectionsConcepts.put(fileName, concepts);
                }
            } catch (RmesException e) {
                logger.error(e.getMessageAndDetails());
            }
        });

        if("odt".equalsIgnoreCase(type)){
            collectionExport.exportMultipleCollectionsAsZipOdt(collections, true, true, true, response, lg, collectionsConcepts);

        } else {
            collectionExport.exportMultipleCollectionsAsZipOds(collections, true, true, true, response, collectionsConcepts);
        }
    }

    private String getFileNameForExport(CollectionForExport collection, ConceptsCollectionsResources.Language lg){
        if (lg == ConceptsCollectionsResources.Language.lg2){
            return CaseUtils.toCamelCase(collection.getPrefLabelLg2(), false) + "-" + collection.getId();
        }
        return CaseUtils.toCamelCase(collection.getPrefLabelLg1(), false) + "-" + collection.getId();
    }
}
