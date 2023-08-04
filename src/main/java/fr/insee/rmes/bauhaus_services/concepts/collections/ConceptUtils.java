package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.utils.XMLUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConceptUtils {
    public static  Map<String, String> convertCollectionInXml(CollectionForExport collection) {
        String collectionXml = XMLUtils.produceXMLResponse(collection);
        Map<String,String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile",  collectionXml.replace("CollectionForExport", "Collection"));
        return xmlContent;
    }
}
