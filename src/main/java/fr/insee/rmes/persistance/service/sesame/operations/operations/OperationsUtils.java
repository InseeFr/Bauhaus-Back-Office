package fr.insee.rmes.persistance.service.sesame.operations.operations;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

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
		JSONArray altLabel = RepositoryGestion.getResponseAsArray(OperationsQueries.altLabel(idOperation));
		if (altLabel.length() != 0) {
			operation.put("altLabel", JSONUtils.extractFieldToArray(altLabel, "altLabel"));
		}
	}

}
