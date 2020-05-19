package fr.insee.rmes.bauhaus_services.sesame.structures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.DSDService;
import fr.insee.rmes.bauhaus_services.sesame.utils.SesameService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.structures.DSDQueries;

@Service
public class DSDImpl  extends SesameService implements DSDService {
	
	static final Logger logger = LogManager.getLogger(DSDImpl.class);
	
	@Autowired 
	DSDUtils dsdUtils;
	
	@Override
	public String getDSDs() throws RmesException {
		logger.info("Starting to get DSDs");
		return repoGestion.getResponseAsArray(DSDQueries.getDSDs()).toString();
	}
	
	@Override
	public String getDSDById(String id) throws RmesException {
		logger.info("Starting to get DSD");
		return repoGestion.getResponseAsObject(DSDQueries.getDSDById(id)).toString();
	}
	
	@Override
	public String getDSDComponents(String dsdId) throws RmesException {
		logger.info("Starting to get components of a DSD");
		return repoGestion.getResponseAsArray(DSDQueries.getDSDComponents(dsdId)).toString();
	}
	
	@Override
	public String getDSDDetailedComponents(String dsdId) throws RmesException {
		logger.info("Starting to get detailed components of a DSD");
		return repoGestion.getResponseAsArray(DSDQueries.getDSDDetailedComponents(dsdId)).toString();
	}
	
	@Override
	public String getDSDComponentById(String dsdId, String componentId) throws RmesException {
		logger.info("Starting to get a DSD component");
		return repoGestion.getResponseAsObject(DSDQueries.getDSDComponentById(dsdId, componentId)).toString();
	}
	
	/**
	 * Create new DSD
	 * @throws RmesException 
	 */
	@Override
	public String setDSD(String body) throws RmesException {
		return dsdUtils.setDSD(body);
	}
	
	/**
	 * Update a DSD
	 * @throws RmesException 
	 */
	@Override
	public String setDSD(String id, String body) throws RmesException {
		return dsdUtils.setDSD(id, body);
	}
}
