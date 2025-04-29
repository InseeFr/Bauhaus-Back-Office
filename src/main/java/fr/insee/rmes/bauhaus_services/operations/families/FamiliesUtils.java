package fr.insee.rmes.bauhaus_services.operations.families;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.*;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.operations.Family;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.operations.families.OpFamiliesQueries;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class FamiliesUtils {

	private static final String CAN_T_READ_REQUEST_BODY = "Can't read request body";

	static final Logger logger = LoggerFactory.getLogger(FamiliesUtils.class);


	boolean familiesRichTextNexStructure;
	final FamOpeSerIndUtils famOpeSerUtils;
	final FamilyPublication familyPublication;
	final ParentUtils ownersUtils;
	final RepositoryGestion repositoryGestion;
	final String lg1;
	final String lg2;
	final StampsRestrictionsService stampsRestrictionsService;
	public FamiliesUtils(@Value("${fr.insee.rmes.bauhaus.feature-flipping.operations.families-rich-text-new-structure}") boolean familiesRichTextNexStructure,
						 FamOpeSerIndUtils famOpeSerUtils,
						 FamilyPublication familyPublication,
						 ParentUtils ownersUtils,
						 RepositoryGestion repositoryGestion,
						 StampsRestrictionsService stampsRestrictionsService,
						 @Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
						 @Value("${fr.insee.rmes.bauhaus.lg2}") String lg2) {

		this.familiesRichTextNexStructure = familiesRichTextNexStructure;
		this.famOpeSerUtils = famOpeSerUtils;
		this.familyPublication = familyPublication;
		this.ownersUtils = ownersUtils;
		this.repositoryGestion = repositoryGestion;
		this.stampsRestrictionsService = stampsRestrictionsService;
		this.lg1 = lg1;
		this.lg2 = lg2;
	}

	public JSONObject getFamilyById(String id) throws RmesException{
		JSONObject family = repositoryGestion.getResponseAsObject(OpFamiliesQueries.familyQuery(id, familiesRichTextNexStructure));
		if (family.isEmpty()) {
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Family "+id+ " not found", "Maybe id is wrong");
		}
		XhtmlToMarkdownUtils.convertJSONObject(family);
		addFamilySeries(id, family);
		addSubjects(id, family);
		return family;
	}


	private void addFamilySeries(String idFamily, JSONObject family) throws RmesException {
		JSONArray series = repositoryGestion.getResponseAsArray(OpFamiliesQueries.getSeries(idFamily));
		if (!series.isEmpty()) {
			family.put("series", series);
		}
	}

	private void addSubjects(String idFamily, JSONObject family) throws RmesException {
		JSONArray subjects = repositoryGestion.getResponseAsArray(OpFamiliesQueries.getSubjects(idFamily));
		if (!subjects.isEmpty()) {
			family.put("subjects", subjects);
		}
	}


	private void validateFamily(Family family) throws RmesException {
		if(repositoryGestion.getResponseAsBoolean(OpFamiliesQueries.checkPrefLabelUnicity(family.getId(), family.getPrefLabelLg1(), lg1))){
			throw new RmesBadRequestException(ErrorCodes.OPERATION_FAMILY_EXISTING_PREF_LABEL_LG1, "This prefLabelLg1 is already used by another family.");
		}
		if(repositoryGestion.getResponseAsBoolean(OpFamiliesQueries.checkPrefLabelUnicity(family.getId(), family.getPrefLabelLg2(), lg2))){
			throw new RmesBadRequestException(ErrorCodes.OPERATION_FAMILY_EXISTING_PREF_LABEL_LG2, "This prefLabelLg2 is already used by another family.");
		}
	}

	public static void verifyBodyToCreateFamily(Family family) throws RmesBadRequestException {
		if(family.prefLabelLg1==null || family.prefLabelLg1.trim().isEmpty()) {
			throw new RmesBadRequestException("Required title not entered by user.");
		}
	}

	public String createFamily(String body) throws RmesException {
		if(!stampsRestrictionsService.canCreateFamily()) {
			throw new RmesUnauthorizedException(ErrorCodes.FAMILY_CREATION_RIGHTS_DENIED, "Only an admin can create a new family.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String id = famOpeSerUtils.createId();

		try {
			Family family = mapper.readValue(body,Family.class);
			verifyBodyToCreateFamily(family);
			family.setId(id);
			family.setCreated(DateUtils.getCurrentDate());
			family.setUpdated(DateUtils.getCurrentDate());
			validateFamily(family);
			createRdfFamily(family, ValidationStatus.UNPUBLISHED);
			logger.info("Create family : {} - {}", id , family.getPrefLabelLg1());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return id;
	}

	public void setFamily(String id, String body) throws RmesException {
		if(!stampsRestrictionsService.canCreateFamily()) {
			throw new RmesUnauthorizedException(ErrorCodes.FAMILY_CREATION_RIGHTS_DENIED, "Only an admin can create or modify a family.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Family family = new Family();
		family.setId(id);
		try {
			family = mapper.readerForUpdating(family).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesNotFoundException(ErrorCodes.FAMILY_INCORRECT_BODY, e.getMessage(), CAN_T_READ_REQUEST_BODY);
		}
		boolean familyExists = famOpeSerUtils.checkIfObjectExists(ObjectType.FAMILY, id);
		if (!familyExists) {
			throw new RmesNotFoundException(ErrorCodes.FAMILY_UNKNOWN_ID, "Family "+id+" doesn't exist", "Can't update non-existant family");
		}
		family.setUpdated(DateUtils.getCurrentDate());
		String status= ownersUtils.getValidationStatus(id);

		validateFamily(family);
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
		IRI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY,family.getId());
		/*Const*/
		model.add(familyURI, RDF.TYPE, INSEE.FAMILY, RdfUtils.operationsGraph());
		/*Required*/
		model.add(familyURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(family.getPrefLabelLg1(), lg1), RdfUtils.operationsGraph());
		model.add(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newStatus.toString()), RdfUtils.operationsGraph());
		/*Optional*/
		RdfUtils.addTripleString(familyURI, SKOS.PREF_LABEL, family.getPrefLabelLg2(), lg2, model, RdfUtils.operationsGraph());
		addAbstractToFamily(family, model, familyURI, RdfUtils.operationsGraph());
		RdfUtils.addTripleDateTime(familyURI, DCTERMS.CREATED, family.getCreated(), model, RdfUtils.operationsGraph());
		RdfUtils.addTripleDateTime(familyURI, DCTERMS.MODIFIED, family.getUpdated(), model, RdfUtils.operationsGraph());

		repositoryGestion.keepHierarchicalOperationLinks(familyURI,model);
		
		repositoryGestion.loadSimpleObject(familyURI, model);
	}


	public void addAbstractToFamily(Family family, Model model, IRI familyURI, Resource graph) throws RmesException {
		RdfUtils.addTripleStringMdToXhtml(familyURI, DCTERMS.ABSTRACT, family.getAbstractLg1(), lg1, model, graph);
		RdfUtils.addTripleStringMdToXhtml(familyURI, DCTERMS.ABSTRACT, family.getAbstractLg2(), lg2, model, graph);

		if(familiesRichTextNexStructure){
			addRichTextToModel(familyURI, family.getAbstractLg1(), lg1, model, graph);
			addRichTextToModel(familyURI, family.getAbstractLg2(), lg2, model, graph);
		}
	}

	private void addRichTextToModel(IRI familyURI, String family, String lang, Model model, Resource graph) throws RmesException {
		IRI iri = RdfUtils.addTripleStringMdToXhtml2(familyURI, DCTERMS.ABSTRACT, family, lang, "resume", model, graph);
		if (iri != null) {
			repositoryGestion.deleteObject(iri, null);
		}
	}


	public void setFamilyValidation(String id) throws  RmesException  {
		Model model = new LinkedHashModel();
		
		if(!stampsRestrictionsService.canCreateFamily()) {
			throw new RmesUnauthorizedException(ErrorCodes.FAMILY_CREATION_RIGHTS_DENIED, "Only an admin can publish a family.");
		}

		familyPublication.publishFamily(id);

		IRI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY, id);
		model.add(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.operationsGraph());
		model.remove(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.operationsGraph());
		model.remove(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.operationsGraph());
		logger.info("Validate family : {}", familyURI);

		repositoryGestion.objectValidation(familyURI, model);
			
	}
}
