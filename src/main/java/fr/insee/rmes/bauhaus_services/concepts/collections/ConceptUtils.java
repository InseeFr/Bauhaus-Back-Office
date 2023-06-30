package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.ConceptsCollectionsResources;
import org.apache.commons.text.CaseUtils;

import java.util.HashMap;
import java.util.Map;

public class ConceptUtils {
   public  static String getFileNameForExport(CollectionForExport collection, ConceptsCollectionsResources.Language lg){
        if (lg == ConceptsCollectionsResources.Language.lg2){
            return FilesUtils.reduceFileNameSize(CaseUtils.toCamelCase(collection.getPrefLabelLg2(), false) + "-" + collection.getId());
        }
        return FilesUtils.reduceFileNameSize(CaseUtils.toCamelCase(collection.getPrefLabelLg1(), false) + "-" + collection.getId());
    }

    public static  Map<String, String> convertCollectionInXml(CollectionForExport collection) {
        String collectionXml = XMLUtils.produceXMLResponse(collection);
        Map<String,String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile",  collectionXml.replace("CollectionForExport", "Collection"));
        return xmlContent;
    }
}
