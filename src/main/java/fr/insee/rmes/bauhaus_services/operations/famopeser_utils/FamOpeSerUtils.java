package fr.insee.rmes.bauhaus_services.operations.famopeser_utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.famOpeSerUtils.FamOpeSerQueries;

@Component
public class FamOpeSerUtils  extends RdfService {

	static final Logger logger = LogManager.getLogger(FamOpeSerUtils.class);

	public String createId() throws RmesException {
		logger.info("Generate famOpeSer id");
		JSONObject json = repoGestion.getResponseAsObject(FamOpeSerQueries.lastId());
		logger.debug("JSON for famOpeSer id : {}", json);
		if (json.length()==0) {return "1000";}
		String id = json.getString(Constants.ID);
		if (id.equals(Constants.UNDEFINED)) {return "1000";}
		return "s" + (Integer.parseInt(id)+1);
	}

	public boolean checkIfObjectExists(ObjectType type, String id) throws RmesException {
		return repoGestion.getResponseAsBoolean(FamOpeSerQueries.checkIfOperationExists(RdfUtils.objectIRI(type, id).toString()));
	}
	
	public String getValidationStatus(String id) throws RmesException{
		try {		
			return repoGestion.getResponseAsObject(FamOpeSerQueries.getPublicationState(id)).getString("state"); }
		catch (JSONException e) {
			return Constants.UNDEFINED;
		}
	}
}
