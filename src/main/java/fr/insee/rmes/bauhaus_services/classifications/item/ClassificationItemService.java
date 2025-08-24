package fr.insee.rmes.bauhaus_services.classifications.item;

import fr.insee.rmes.domain.exceptions.RmesException;

public interface ClassificationItemService {
    String getClassificationItems(String id) throws RmesException;

    String getClassificationItem(String classificationid, String itemId) throws RmesException;

    String getClassificationItemNotes(String classificationid, String itemId, int conceptVersion) throws RmesException;

    String getClassificationItemNarrowers(String classificationid, String itemId) throws RmesException;

    void updateClassificationItem(String classificationId, String itemId, String body) throws RmesException;

}
