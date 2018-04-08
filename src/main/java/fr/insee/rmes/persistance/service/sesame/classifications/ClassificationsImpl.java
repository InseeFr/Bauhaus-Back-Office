package fr.insee.rmes.persistance.service.sesame.classifications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.rmes.persistance.service.ClassificationsService;
import fr.insee.rmes.persistance.service.sesame.classifications.classifications.ClassificationsQueries;
import fr.insee.rmes.persistance.service.sesame.classifications.families.FamiliesQueries;
import fr.insee.rmes.persistance.service.sesame.classifications.series.SeriesQueries;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class ClassificationsImpl implements ClassificationsService {

	final static Logger logger = LogManager.getLogger(ClassificationsImpl.class);
	
	@Override
	public String getFamilies() {
		logger.info("Starting to get classification families");
		return RepositoryGestion.getResponseAsArray(FamiliesQueries.familiesQuery()).toString();
	}
	
	@Override
	public String getFamily(String id) {
		logger.info("Starting to get classification family");
		return RepositoryGestion.getResponseAsObject(FamiliesQueries.familyQuery(id)).toString();
	}
	
	@Override
	public String getFamilyMembers(String id) {
		logger.info("Starting to get classification family members");
		return RepositoryGestion.getResponseAsArray(FamiliesQueries.familyMembersQuery(id)).toString();
	}
	
	@Override
	public String getSeries() {
		logger.info("Starting to get classifications series");
		return RepositoryGestion.getResponseAsArray(SeriesQueries.seriesQuery()).toString();
	}
	
	@Override
	public String getOneSeries(String id) {
		logger.info("Starting to get a classification series");
		return RepositoryGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id)).toString();
	}
	
	@Override
	public String getSeriesMembers(String id) {
		logger.info("Starting to get members of a classification series");
		return RepositoryGestion.getResponseAsArray(SeriesQueries.seriesMembersQuery(id)).toString();
	}
	
	@Override
	public String getClassifications() {
		logger.info("Starting to get classifications");
		return RepositoryGestion.getResponseAsArray(ClassificationsQueries.classificationsQuery()).toString();
	}
	
	@Override
	public String getClassification(String id) {
		logger.info("Starting to get a classification scheme");
		return RepositoryGestion.getResponseAsObject(ClassificationsQueries.classificationQuery(id)).toString();
	}
	
	@Override
	public String getClassificationLevels(String id) {
		logger.info("Starting to get levels of a classification scheme");
		return RepositoryGestion.getResponseAsArray(ClassificationsQueries.classificationLevelsQuery(id)).toString();
	}

}
