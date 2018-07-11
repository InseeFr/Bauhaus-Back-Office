package fr.insee.rmes.persistance.service.sesame.operations.operations;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.utils.JSONUtils;

@Component
public class OperationsUtils {

	public JSONObject getOperationById(String id) {
		JSONObject operation = RepositoryGestion.getResponseAsObject(OperationsQueries.operationQuery(id));
		addOperationAltLabel(id, operation);
		return operation;
	}


	private void addOperationAltLabel(String idOperation, JSONObject operation) {
		JSONArray altLabelLg1 = RepositoryGestion.getResponseAsArray(OperationsQueries.altLabel(idOperation, Config.LG1));
		JSONArray altLabelLg2 = RepositoryGestion.getResponseAsArray(OperationsQueries.altLabel(idOperation, Config.LG2));
		if (altLabelLg1.length() != 0) {
			operation.put("altLabelLg1", JSONUtils.extractFieldToArray(altLabelLg1, "altLabel"));
		}
		if (altLabelLg2.length() != 0) {
			operation.put("altLabelLg2", JSONUtils.extractFieldToArray(altLabelLg2, "altLabel"));
		}
	}

}
