package fr.insee.rmes.persistance.service.sesame.classifications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.rmes.persistance.service.ClassificationsService;
import fr.insee.rmes.persistance.service.sesame.classifications.families.FamiliesQueries;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class ClassificationsImpl implements ClassificationsService {

	final static Logger logger = LogManager.getLogger(ClassificationsImpl.class);
	
	@Override
	public String getFamilies() {
		logger.info("Starting to get classification families");
		return RepositoryGestion.getResponseAsArray(FamiliesQueries.familiesQuery()).toString();
	}
	
}
