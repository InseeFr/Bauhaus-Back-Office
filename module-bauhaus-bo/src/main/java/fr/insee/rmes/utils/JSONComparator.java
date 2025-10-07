package fr.insee.rmes.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class JSONComparator implements Comparator<JSONObject> {
	
	static final Logger logger = LoggerFactory.getLogger(JSONComparator.class);

	
    private String fieldToCompare;

    public JSONComparator(String fieldToCompare) {
        this.fieldToCompare = fieldToCompare;
    }

    @Override
    public int compare(JSONObject o1, JSONObject o2) {
        String id1 = "";
        String id2 = "";
        try {
            id1 = o1.getString(this.fieldToCompare);
            id2 = o2.getString(this.fieldToCompare);
        } catch (JSONException e) {
        	logger.error("Error comparing json", e);
        }

        return id1.toLowerCase().compareTo(id2.toLowerCase());
    }
}
