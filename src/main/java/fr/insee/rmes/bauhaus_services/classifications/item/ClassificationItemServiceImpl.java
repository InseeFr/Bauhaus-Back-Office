package fr.insee.rmes.bauhaus_services.classifications.item;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.classification.ClassificationItem;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.ItemsQueries;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ClassificationItemServiceImpl extends RdfService implements ClassificationItemService {
    private static final String CAN_T_READ_REQUEST_BODY = "Can't read request body";

    @Autowired
    ClassificationItemRepository classificationItemUtils;


    static final Logger logger = LoggerFactory.getLogger(ClassificationItemServiceImpl.class);

    @Override
    public String getClassificationItems(String id) throws RmesException{
        logger.info("Starting to get a classification scheme");
        return repoGestion.getResponseAsArray(ClassificationsQueries.classificationItemsQuery(id)).toString();
    }

    @Override
    public String getClassificationItem(String classificationId, String itemId) throws RmesException{
        logger.info("Starting to get classification item {} from {}", itemId, classificationId);
        JSONObject item = repoGestion.getResponseAsObject(ItemsQueries.itemQuery(classificationId, itemId));
        JSONArray altLabels = repoGestion.getResponseAsArray(ItemsQueries.itemAltQuery(classificationId, itemId));
        if(!altLabels.isEmpty()) {
            item.put("altLabels", altLabels);
        }
        return item.toString();
    }

    @Override
    public String getClassificationItemNotes(String classificationId, String itemId, int conceptVersion)throws RmesException {
        logger.info("Starting to get classification item notes {} from {}", itemId, classificationId);
        JSONObject classificationItemNotes = repoGestion.getResponseAsObject(ItemsQueries.itemNotesQuery(classificationId, itemId, conceptVersion));
        XhtmlToMarkdownUtils.convertJSONObject(classificationItemNotes);
        return classificationItemNotes.toString();
    }

    @Override
    public String getClassificationItemNarrowers(String classificationId, String itemId) throws RmesException {
        logger.info("Starting to get classification item members {} from {}", itemId, classificationId);
        return repoGestion.getResponseAsArray(ItemsQueries.itemNarrowersQuery(classificationId, itemId)).toString();
    }

    @Override
    public void updateClassificationItem(String classificationId, String itemId, String body) throws RmesException {
        logger.info("Updating item {} for classification {}", itemId, classificationId);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ClassificationItem item = new ClassificationItem();
        item.setId(itemId);
        try {
            item = mapper.readerForUpdating(item).readValue(body);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RmesNotFoundException(ErrorCodes.CLASSIFICATION_INCORRECT_BODY, e.getMessage(), CAN_T_READ_REQUEST_BODY);
        }


        String itemUri = repoGestion.getResponseAsObject(ClassificationsQueries.classificationItemQueryUri(classificationId, itemId)).getString("item");
        classificationItemUtils.updateClassificationItem(item, itemUri, classificationId);
    }
}
