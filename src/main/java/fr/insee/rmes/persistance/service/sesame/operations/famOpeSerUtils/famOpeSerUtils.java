package fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.operations.operations.OperationsUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class famOpeSerUtils {

	final static Logger logger = LogManager.getLogger(OperationsUtils.class);

	public static String createId() throws RmesException {
		logger.info("Generate famOpeSer id");
		JSONObject json = RepositoryGestion.getResponseAsObject(famOpeSerQueries.lastId());
		logger.debug("JSON for famOpeSer id : " + json);
		if (json.length()==0) {return null;}
		String id = json.getString("id");
		if (id.equals("undefined")) {return null;}
		int ID = Integer.parseInt(id)+1;
		return "s" + ID;
	}

	
	
}
