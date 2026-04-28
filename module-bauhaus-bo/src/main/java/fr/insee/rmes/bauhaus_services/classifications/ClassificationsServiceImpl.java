package fr.insee.rmes.bauhaus_services.classifications;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.classifications.nomenclatures.model.Classification;
import fr.insee.rmes.modules.classifications.nomenclatures.model.PartialClassification;
import fr.insee.rmes.modules.classifications.families.model.PartialClassificationFamily;
import fr.insee.rmes.modules.classifications.series.model.PartialClassificationSeries;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.classifications.*;
import fr.insee.rmes.utils.DiacriticSorter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ClassificationsServiceImpl implements ClassificationsService {
	private static final String CAN_T_READ_REQUEST_BODY = "Can't read request body";

	private final RepositoryGestion repoGestion;
	private final ClassificationRepository classificationUtils;
	private final ClassificationPublication classificationPublication;
	private final ClassificationsQueries classificationsQueries;
	private final ClassificationLevelsQueries classificationLevelsQueries;
	private final ClassificationSeriesQueries classificationSeriesQueries;
	private final ClassificationFamiliesQueries classificationFamiliesQueries;
	private final ClassificationCorrespondencesQueries classificationCorrespondencesQueries;

	static final Logger logger = LoggerFactory.getLogger(ClassificationsServiceImpl.class);

	public ClassificationsServiceImpl(RepositoryGestion repoGestion, ClassificationRepository classificationUtils, ClassificationPublication classificationPublication, ClassificationsQueries classificationsQueries, ClassificationLevelsQueries classificationLevelsQueries, ClassificationSeriesQueries classificationSeriesQueries, ClassificationFamiliesQueries classificationFamiliesQueries, ClassificationCorrespondencesQueries classificationCorrespondencesQueries) {
		this.repoGestion = repoGestion;
		this.classificationUtils = classificationUtils;
		this.classificationPublication = classificationPublication;
		this.classificationsQueries = classificationsQueries;
		this.classificationLevelsQueries = classificationLevelsQueries;
		this.classificationSeriesQueries = classificationSeriesQueries;
		this.classificationFamiliesQueries = classificationFamiliesQueries;
		this.classificationCorrespondencesQueries = classificationCorrespondencesQueries;
	}

	@Override
	public List<PartialClassificationFamily> getFamilies() throws RmesException {
		logger.info("Starting to get classification families");
		var families = repoGestion.getResponseAsArray(classificationFamiliesQueries.familiesQuery());

		return DiacriticSorter.sort(families,
				PartialClassificationFamily[].class,
				PartialClassificationFamily::label);
	}
	
	@Override
	public String getFamily(String id) throws RmesException {
		logger.info("Starting to get classification family");
		return repoGestion.getResponseAsObject(classificationFamiliesQueries.familyQuery(id)).toString();
	}
	
	@Override
	public String getFamilyMembers(String id) throws RmesException {
		logger.info("Starting to get classification family members");
		return repoGestion.getResponseAsArray(classificationFamiliesQueries.familyMembersQuery(id)).toString();
	}
	
	@Override
	public List<PartialClassificationSeries> getSeries() throws RmesException {
		logger.info("Starting to get classifications series");
		var series = repoGestion.getResponseAsArray(classificationSeriesQueries.seriesQuery());

		return DiacriticSorter.sortGroupingByIdConcatenatingAltLabels(series,
				PartialClassificationSeries[].class,
				PartialClassificationSeries::label
		);
	}
	
	@Override
	public String getOneSeries(String id) throws RmesException {
		logger.info("Starting to get a classification series");
		return repoGestion.getResponseAsObject(classificationSeriesQueries.oneSeriesQuery(id)).toString();
	}
	
	@Override
	public String getSeriesMembers(String id) throws RmesException {
		logger.info("Starting to get members of a classification series");
		return repoGestion.getResponseAsArray(classificationSeriesQueries.seriesMembersQuery(id)).toString();
	}
	
	@Override
	public List<PartialClassification> getClassifications() throws RmesException {
		logger.info("Starting to get classifications");
		var collections = repoGestion.getResponseAsArray(classificationsQueries.classificationsQuery());

		return DiacriticSorter.sortGroupingByIdConcatenatingAltLabels(collections,
				PartialClassification[].class,
				PartialClassification::label);
	}
	
	@Override
	public String getClassification(String id) throws RmesException{
		logger.info("Starting to get a classification scheme");
		JSONObject classification = repoGestion.getResponseAsObject(classificationsQueries.classificationQuery(id));
		return classification.toString();
	}

	@Override
	public void updateClassification(String id, String body) throws RmesException {
		logger.info("Starting to update the classification {}", id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Classification classification = new Classification();
		classification.setId(id);
		try {
			classification = mapper.readerForUpdating(classification).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesNotFoundException(ErrorCodes.CLASSIFICATION_INCORRECT_BODY, e.getMessage(), CAN_T_READ_REQUEST_BODY);
		}
		String uri = repoGestion.getResponseAsObject(classificationsQueries.classificationQueryUri(classification.getId())).getString("iri");

		classificationUtils.updateClassification(classification, uri);
	}

	@Override
	public String getClassificationLevels(String id) throws RmesException{
		logger.info("Starting to get levels of a classification scheme");
		return repoGestion.getResponseAsArray(classificationLevelsQueries.levelsQuery(id)).toString();
	}
	
	@Override
	public String getClassificationLevel(String classificationId, String levelId) throws RmesException{
		logger.info("Starting to get a classification level");
		return repoGestion.getResponseAsObject(classificationLevelsQueries.levelQuery(classificationId, levelId)).toString();
	}
	
	@Override
	public String getClassificationLevelMembers(String classificationId, String levelId)throws RmesException {
		logger.info("Starting to get classification level members");
		return repoGestion.getResponseAsArray(classificationLevelsQueries.levelMembersQuery(classificationId, levelId)).toString();
	}
	
	@Override
	public String getCorrespondences() throws RmesException{
		logger.info("Starting to get correspondences");
		return repoGestion.getResponseAsArray(classificationCorrespondencesQueries.correspondencesQuery()).toString();
	}

	@Override
	public String getCorrespondence(String id) throws RmesException{
		logger.info("Starting to get a correspondence scheme : {}" , id);
		return repoGestion.getResponseAsObject(classificationCorrespondencesQueries.correspondenceQuery(id)).toString();
	}
	
	@Override
	public String getCorrespondenceAssociations(String id) throws RmesException{
		logger.info("Starting to get correspondence associations : {}" , id);
		return repoGestion.getResponseAsArray(classificationCorrespondencesQueries.correspondenceAssociationsQuery(id)).toString();
	}
	
	@Override
	public String getCorrespondenceAssociation(String correspondenceId, String associationId) throws RmesException{
		logger.info("Starting to get correspondence association : {} - {}" , correspondenceId , associationId);
		return repoGestion.getResponseAsObject(classificationCorrespondencesQueries.correspondenceAssociationQuery(correspondenceId, associationId)).toString();
	}

	@Override
	public void setClassificationValidation(String classificationId) throws RmesException {
		//GET graph
		JSONObject listGraph = repoGestion.getResponseAsObject(classificationsQueries.getGraphUriById(classificationId));
		logger.debug("JSON for listGraph id : {}", listGraph);
		if (listGraph.isEmpty()) {throw new RmesNotFoundException(ErrorCodes.CLASSIFICATION_UNKNOWN_ID, "Classification not found", classificationId);}
		String graph = listGraph.getString("graph");
		String classifUriString = listGraph.getString(Constants.URI);
		Resource graphIri = RdfUtils.createIRI(graph);


		//PUBLISH
		classificationPublication.publishClassification(graphIri);

		//UPDATE GESTION TO MARK AS PUBLISHED
		Model model = new LinkedHashModel();
		IRI classificationURI = RdfUtils.toURI(classifUriString);
		model.add(classificationURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), graphIri);
		model.remove(classificationURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), graphIri);
		model.remove(classificationURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), graphIri);
		logger.info("Validate classification : {}", classifUriString);
		repoGestion.objectValidation(classificationURI, model);

	}
}
