package fr.insee.rmes.persistance.service.sesame.operations.operations;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class OperationsUtils {

	public JSONObject getOperationById(String id) {
		return RepositoryGestion.getResponseAsObject(OperationsQueries.operationQuery(id));
	}



}
