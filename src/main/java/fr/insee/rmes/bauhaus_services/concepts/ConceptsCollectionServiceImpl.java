package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.ConceptsResources;
import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConceptsCollectionServiceImpl implements ConceptsCollectionService {
    static final Logger logger = LogManager.getLogger(ConceptsCollectionServiceImpl.class);

    @Autowired
    CollectionExportBuilder collectionExport;

    private Map<String, String> convertCollectionInXml(CollectionForExport collection) {
        String collectionXml = XMLUtils.produceXMLResponse(collection);
        Map<String,String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile",  collectionXml.replace("CollectionForExport", "Collection"));
        return xmlContent;
    }

    @Override
    public ResponseEntity<?> getCollectionExportODT(String id, String accept, ConceptsResources.Language lg) throws RmesException {
        logger.info("Exporting a collection {} to odt", id);

        try {
            CollectionForExport collection = collectionExport.getCollectionData(id);
            Map<String, String> xmlContent = convertCollectionInXml(collection);
            String fileName = getFileNameForExport(collection, lg);
            return collectionExport.exportAsResponseODT(fileName,xmlContent,true,true,true, lg);
        } catch (RmesException e){
            return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
        }
    }

    @Override
    public ResponseEntity<?> getCollectionExportODS(String id, String accept) throws RmesException {
        logger.info("Exporting a collection {} to ods", id);

        try {
            CollectionForExport collection = collectionExport.getCollectionData(id);
            Map<String, String> xmlContent = convertCollectionInXml(collection);
            String fileName = getFileNameForExport(collection, null);
            return collectionExport.exportAsResponseODS(fileName,xmlContent,true,true,true);
        } catch (RmesException e){
            return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
        }
    }

    @Override
    public void exportZipCollection(String ids, String acceptHeader, HttpServletResponse response, ConceptsResources.Language lg, String type) throws RmesException {
        Map<String, Map<String, String>> collections = new HashMap<>();
        Arrays.asList(ids.split(",")).forEach(id -> {
            try {
                CollectionForExport collection = collectionExport.getCollectionData(id);
                Map<String, String> xmlContent = convertCollectionInXml(collection);
                String fileName = getFileNameForExport(collection, lg);
                collections.put(fileName, xmlContent);


            } catch (RmesException e) {
                logger.error(e.getMessageAndDetails());
            }
        });

        if("odt".equalsIgnoreCase(type)){
            collectionExport.exportMultipleCollectionsAsZipOdt(collections, true, true, true, response, lg);

        } else {
            collectionExport.exportMultipleCollectionsAsZipOds(collections, true, true, true, response);
        }
    }

    private String getFileNameForExport(CollectionForExport collection, ConceptsResources.Language lg){
        if (lg == ConceptsResources.Language.lg2){
            return CaseUtils.toCamelCase(collection.getPrefLabelLg2(), false) + "-" + collection.getId();
        }
        return CaseUtils.toCamelCase(collection.getPrefLabelLg1(), false) + "-" + collection.getId();
    }
}
