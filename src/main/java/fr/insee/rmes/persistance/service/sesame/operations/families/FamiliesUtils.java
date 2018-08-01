package fr.insee.rmes.persistance.service.sesame.operations.families;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class FamiliesUtils {


	public JSONObject getFamilyById(String id){
		JSONObject family = RepositoryGestion.getResponseAsObject(FamiliesQueries.familyQuery(id));
		addFamilySeries(id, family);
		addSubjects(id, family);
		return family;
	}


	private void addFamilySeries(String idFamily, JSONObject family) {
		JSONArray series = RepositoryGestion.getResponseAsArray(FamiliesQueries.getSeries(idFamily));
		if (series.length() != 0) {
			family.put("series", series);
		}
	}

	private void addSubjects(String idFamily, JSONObject family) {
		JSONArray subjects = RepositoryGestion.getResponseAsArray(FamiliesQueries.getSubjects(idFamily));
		if (subjects.length() != 0) {
			family.put("subjects", subjects);
		}
	}
}
