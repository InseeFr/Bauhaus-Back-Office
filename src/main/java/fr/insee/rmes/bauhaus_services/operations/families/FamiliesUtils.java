package fr.insee.rmes.bauhaus_services.operations.families;

import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.operations.Family;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.operations.families.FamiliesQueries;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

@Component
public class FamiliesUtils  extends RdfService {

	private static final String CAN_T_READ_REQUEST_BODY = "Can't read request body";

	static final Logger logger = LogManager.getLogger(FamiliesUtils.class);

	@Autowired
	static FamOpeSerUtils famOpeSerUtils;
	
	@Autowired
	FamilyPublication familyPublication;

/*READ*/
	public JSONObject getFamilyById(String id) throws RmesException{
		JSONObject family = repoGestion.getResponseAsObject(FamiliesQueries.familyQuery(id));
		if (family.length()==0) {
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Family "+id+ " not found", "Maybe id is wrong");
		}
		XhtmlToMarkdownUtils.convertJSONObject(family);
		addFamilySeries(id, family);
		addSubjects(id, family);
		return family;
	}


	private void addFamilySeries(String idFamily, JSONObject family) throws RmesException {
		JSONArray series = repoGestion.getResponseAsArray(FamiliesQueries.getSeries(idFamily));
		if (series.length() != 0) {
			family.put("series", series);
		}
	}

	private void addSubjects(String idFamily, JSONObject family) throws RmesException {
		JSONArray subjects = repoGestion.getResponseAsArray(FamiliesQueries.getSubjects(idFamily));
		if (subjects.length() != 0) {
			family.put("subjects", subjects);
		}
	}


/*WRITE*/
	public void setFamily(String id, String body) throws RmesException {
		if(!stampsRestrictionsService.canCreateFamily()) {
			throw new RmesUnauthorizedException(ErrorCodes.FAMILY_CREATION_RIGHTS_DENIED, "Only an admin can modify a family.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Family family = new Family(id);
		try {
			family = mapper.readerForUpdating(family).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesNotFoundException(ErrorCodes.FAMILY_INCORRECT_BODY, e.getMessage(), CAN_T_READ_REQUEST_BODY);
		}
		boolean familyExists = famOpeSerUtils.checkIfObjectExists(ObjectType.FAMILY,id);
		if (!familyExists) {
			throw new RmesNotFoundException(ErrorCodes.FAMILY_UNKNOWN_ID, "Family "+id+" doesn't exist", "Can't update non-existant family");
		}

		String status= famOpeSerUtils.getValidationStatus(id);
		if(status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
			createRdfFamily(family,ValidationStatus.UNPUBLISHED);
		} else {
			createRdfFamily(family,ValidationStatus.MODIFIED);
		}
		logger.info("Update family : {} - {}" , family.getId() , family.getPrefLabelLg1());
		
	}

	public void createRdfFamily(Family family, ValidationStatus newStatus) throws RmesException {
		Model model = new LinkedHashModel();
		if (family == null || StringUtils.isEmpty(family.id)) {
			throw new RmesNotFoundException(ErrorCodes.FAMILY_UNKNOWN_ID, "No id found", CAN_T_READ_REQUEST_BODY);
		}
		if (StringUtils.isEmpty(family.getPrefLabelLg1())) {
			throw new RmesNotFoundException(ErrorCodes.FAMILY_INCORRECT_BODY, "prefLabelLg1 not found", CAN_T_READ_REQUEST_BODY);
		}
		URI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY,family.getId());
		/*Const*/
		model.add(familyURI, RDF.TYPE, INSEE.FAMILY, RdfUtils.operationsGraph());
		/*Required*/
		model.add(familyURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(family.getPrefLabelLg1(), Config.LG1), RdfUtils.operationsGraph());
		model.add(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newStatus.toString()), RdfUtils.operationsGraph());
		/*Optional*/
		RdfUtils.addTripleString(familyURI, SKOS.PREF_LABEL, family.getPrefLabelLg2(), Config.LG2, model, RdfUtils.operationsGraph());
		RdfUtils.addTripleStringMdToXhtml(familyURI, DCTERMS.ABSTRACT, family.getAbstractLg1(), Config.LG1, model, RdfUtils.operationsGraph());
		RdfUtils.addTripleStringMdToXhtml(familyURI, DCTERMS.ABSTRACT, family.getAbstractLg2(), Config.LG2, model, RdfUtils.operationsGraph());

		repoGestion.keepHierarchicalOperationLinks(familyURI,model);
		
		repoGestion.loadSimpleObject(familyURI, model, null);
	}


	public String createFamily(String body) throws RmesException {
		if(!stampsRestrictionsService.canCreateFamily()) {
			throw new RmesUnauthorizedException(ErrorCodes.FAMILY_CREATION_RIGHTS_DENIED, "Only an admin can create a new family.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String id = famOpeSerUtils.createId();
		Family family = new Family(id);
		try {
			family = mapper.readValue(body,Family.class);
			family.id = id;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfFamily(family,ValidationStatus.UNPUBLISHED);
		logger.info("Create family : {} - {}", id , family.getPrefLabelLg1());
		return id;

	}


	public String setFamilyValidation(String id) throws  RmesException  {
		Model model = new LinkedHashModel();
		
		if(!stampsRestrictionsService.canCreateFamily()) {
			throw new RmesUnauthorizedException(ErrorCodes.FAMILY_CREATION_RIGHTS_DENIED, "Only an admin can publish a family.");
		}

			familyPublication.publishFamily(id);
		
			URI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY, id);
			model.add(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.operationsGraph());
			model.remove(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.operationsGraph());
			model.remove(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.operationsGraph());
			logger.info("Validate family : {}", familyURI);

			repoGestion.objectValidation(familyURI, model);
			
		return id;
	}
	
	
}
