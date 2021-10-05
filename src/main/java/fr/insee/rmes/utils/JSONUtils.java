package fr.insee.rmes.utils;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	
	/**
	 * Transform an array to a string with " - " separator
	 * @param jsonArray
	 * @return
	 */
	public static String jsonArrayOfStringToString(JSONArray jsonArray) {
		if (jsonArray.length() == 1) {
			return jsonArray.getString(0);
		}
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
	        if(!obj.getString((keys.next())).isEmpty()) {
				return false;
			}
	    }
	    return true;
	}

	/**
	 * Transform an array to a list of strings
	 * @param jsonArray
	 * @return
	 */
	public static List<String> jsonArrayToList(JSONArray jsonArray) {
		return IntStream.range(0, jsonArray.length())
		        .mapToObj(jsonArray::get)
		        .map(Object::toString)
		        .collect(Collectors.toList());
	}
	
	  private JSONUtils() {
		    throw new IllegalStateException("Utility class");
	}

	
}
