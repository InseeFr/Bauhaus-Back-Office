package fr.insee.rmes.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JSONUtils {


	public static JSONArray extractFieldToArray(JSONArray jsonA, String field) {
		JSONArray res = new JSONArray();
		stream(jsonA).forEach(object -> res.put(object.getString(field)));
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


	private static IntStream generateIntStreamBasedOnJsonArray(JSONArray array) {
		return IntStream.range(0, array.length());
	}

	public static Stream<JSONObject> stream(JSONArray array) {
		return generateIntStreamBasedOnJsonArray(array).mapToObj(array::getJSONObject);
	}

	/**
	 * Transform an array to a list of strings
	 */
	public static List<String> jsonArrayToList(JSONArray array) {
		return generateIntStreamBasedOnJsonArray(array)
		        .mapToObj(array::get)
		        .map(Object::toString)
		        .toList();
	}
	
	  private JSONUtils() {
		    throw new IllegalStateException("Utility class");
	}

	
}
