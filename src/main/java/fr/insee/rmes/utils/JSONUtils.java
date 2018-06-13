package fr.insee.rmes.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONUtils {
	
	public static JSONArray extractFieldToArray(JSONArray jsonA, String field) {
		JSONArray res = new JSONArray();
		for(Object o: jsonA){
			res.put(((JSONObject) o).getString(field));
		}
		return res;
	}

}
