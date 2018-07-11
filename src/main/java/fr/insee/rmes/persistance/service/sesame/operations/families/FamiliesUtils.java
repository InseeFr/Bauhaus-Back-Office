package fr.insee.rmes.persistance.service.sesame.operations.families;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class FamiliesUtils {


	public JSONObject getFamilyById(String id){
		JSONObject family = RepositoryGestion.getResponseAsObject(FamiliesQueries.familyQuery(id));
		addSeries(id, family);
		return family;
	}


	private void addSeries(String idFamily, JSONObject family) {
		JSONArray series = RepositoryGestion.getResponseAsArray(FamiliesQueries.getSeries(idFamily));
		if (series.length() != 0) {
			family.put("series", series);
		}
	}
}
