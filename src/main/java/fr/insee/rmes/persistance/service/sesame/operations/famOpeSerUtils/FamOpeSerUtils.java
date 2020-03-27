package fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.Constants;
import fr.insee.rmes.persistance.service.sesame.operations.operations.OperationsUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;
import fr.insee.rmes.persistance.sparqlQueries.operations.famOpeSerUtils.FamOpeSerQueries;

@Component
public class FamOpeSerUtils {

	final static Logger logger = LogManager.getLogger(OperationsUtils.class);

	public static String createId() throws RmesException {
		logger.info("Generate famOpeSer id");
		JSONObject json = RepositoryGestion.getResponseAsObject(FamOpeSerQueries.lastId());
		logger.debug("JSON for famOpeSer id : " + json);
		if (json.length()==0) {return "1000";}
		String id = json.getString(Constants.ID);
		if (id.equals("undefined")) {return "1000";}
		int ID = Integer.parseInt(id)+1;
		return "s" + ID;
	}

	public static boolean checkIfObjectExists(ObjectType type, String id) throws RmesException {
		return RepositoryGestion.getResponseAsBoolean(FamOpeSerQueries.checkIfOperationExists(SesameUtils.objectIRI(type, id).toString()));
	}
	
	public static String getValidationStatus(String id) throws RmesException{
		try {		
			return RepositoryGestion.getResponseAsObject(FamOpeSerQueries.getPublicationState(id)).getString("state"); }
		catch (JSONException e) {
			return Constants.UNDEFINED;
		}
	}
}
