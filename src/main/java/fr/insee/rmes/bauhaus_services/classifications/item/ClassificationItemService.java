package fr.insee.rmes.bauhaus_services.classifications.item;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;

public interface ClassificationItemService {
    public String getClassificationItems(String id) throws RmesException;

    public String getClassificationItem(String classificationid, String itemId) throws RmesException;

    public String getClassificationItemNotes(String classificationid, String itemId, int conceptVersion) throws RmesException;

    public String getClassificationItemNarrowers(String classificationid, String itemId) throws RmesException;

    public void updateClassificationItem(String classificationId, String itemId, String body) throws RmesException;

}
