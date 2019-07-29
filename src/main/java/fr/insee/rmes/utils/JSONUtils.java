package fr.insee.rmes.utils;

import java.util.Iterator;

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
	
	public static String jsonArrayOfStringToString(JSONArray jsonArray) {
		if (jsonArray.length() == 1) return jsonArray.getString(0);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < jsonArray.length()-1; i++) {
			sb.append(jsonArray.getString(i) + " - ");
		}
		sb.append(jsonArray.getString(jsonArray.length()-1));
		return sb.toString();
	}
	
	public static boolean isEmpty(JSONObject obj) {
		Iterator<String> keys = obj.keys();
		while(keys.hasNext())  {
	        if(!obj.getString((String)(keys.next())).isEmpty())
	            return false;
	    }
	    return true;
	}

}
