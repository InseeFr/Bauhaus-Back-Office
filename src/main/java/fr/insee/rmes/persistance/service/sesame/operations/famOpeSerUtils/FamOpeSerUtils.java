package fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openrdf.model.Resource;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.operations.operations.OperationsUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class FamOpeSerUtils {

	final static Logger logger = LogManager.getLogger(OperationsUtils.class);

	public static String createId() throws RmesException {
		logger.info("Generate famOpeSer id");
		JSONObject json = RepositoryGestion.getResponseAsObject(FamOpeSerQueries.lastId());
		logger.debug("JSON for famOpeSer id : " + json);
		if (json.length()==0) {return null;}
		String id = json.getString("id");
		if (id.equals("undefined")) {return null;}
		int ID = Integer.parseInt(id)+1;
		return "s" + ID;
	}

	public static Boolean checkIfObjectExists(ObjectType type, String id) throws RmesException {
		return RepositoryGestion.getResponseAsBoolean(FamOpeSerQueries.checkIfOperationExists(SesameUtils.objectIRI(type, id).toString()));
	}
	
	public static Resource tranformBaseURIToPublish(Resource resource) {
		String newResource = resource.toString().replace(Config.BASE_URI_GESTION, Config.BASE_URI_PUBLICATION);
		return SesameUtils.toURI(newResource);

	}
}
