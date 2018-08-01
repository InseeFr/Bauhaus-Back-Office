package fr.insee.rmes.persistance.service.sesame.operations.operations;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class OperationsUtils {

	public JSONObject getOperationById(String id) {
		JSONObject operation = RepositoryGestion.getResponseAsObject(OperationsQueries.operationQuery(id));
		getOperationSeries(id, operation);
		return operation;
	}

	private void getOperationSeries(String id, JSONObject operation) {
		JSONObject series = RepositoryGestion.getResponseAsObject(OperationsQueries.seriesQuery(id));
		operation.put("series", series);
	}


}
