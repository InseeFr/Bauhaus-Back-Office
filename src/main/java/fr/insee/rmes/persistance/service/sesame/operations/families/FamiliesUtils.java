package fr.insee.rmes.persistance.service.sesame.operations.families;

import java.io.IOException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils.FamOpeSerQueries;
import fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class FamiliesUtils {

	final static Logger logger = LogManager.getLogger(FamiliesUtils.class);

/*READ*/
	public JSONObject getFamilyById(String id) throws RmesException{
		JSONObject family = RepositoryGestion.getResponseAsObject(FamiliesQueries.familyQuery(id));
		if (family.length()==0) throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Family "+id+ " not found", "Maybe id is wrong");
		addFamilySeries(id, family);
		addSubjects(id, family);
		return family;
	}


	private void addFamilySeries(String idFamily, JSONObject family) throws RmesException {
		JSONArray series = RepositoryGestion.getResponseAsArray(FamiliesQueries.getSeries(idFamily));
		if (series.length() != 0) {
			family.put("series", series);
		}
	}

	private void addSubjects(String idFamily, JSONObject family) throws RmesException {
		JSONArray subjects = RepositoryGestion.getResponseAsArray(FamiliesQueries.getSubjects(idFamily));
		if (subjects.length() != 0) {
			family.put("subjects", subjects);
		}
	}


/*WRITE*/
	public void setFamily(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Family family = new Family(id);
		try {
			family = mapper.readerForUpdating(family).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesNotFoundException( e.getMessage(), "Can't read request body");
		}
		boolean familyExists = FamOpeSerUtils.checkIfObjectExists(ObjectType.FAMILY,id);
		if (!familyExists) {
			throw new RmesException(HttpStatus.SC_NOT_ACCEPTABLE, "Family "+id+" doesn't exist", "Can't update non-existant family");
		}

		String status=FamOpeSerUtils.getValidationStatus(id);
		if(status.equals(INSEE.UNPUBLISHED) | status.equals("UNDEFINED")) {
			createRdfFamily(family,INSEE.UNPUBLISHED);
		}
		else 	createRdfFamily(family,INSEE.MODIFIED);
		logger.info("Update family : " + family.getId() + " - " + family.getPrefLabelLg1());
		
	}

	public void createRdfFamily(Family family, String newStatus) throws RmesException {
		Model model = new LinkedHashModel();
		if (family == null || StringUtils.isEmpty(family.id)) {
			throw new RmesNotFoundException( "No id found", "Can't read request body");
		}
		if (StringUtils.isEmpty(family.getPrefLabelLg1())) {
			throw new RmesNotFoundException( "prefLabelLg1 not found", "Can't read request body");
		}
		URI familyURI = SesameUtils.objectIRI(ObjectType.FAMILY,family.getId());
		/*Const*/
		model.add(familyURI, RDF.TYPE, INSEE.FAMILY, SesameUtils.operationsGraph());
		/*Required*/
		model.add(familyURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(family.getPrefLabelLg1(), Config.LG1), SesameUtils.operationsGraph());
		model.add(familyURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(newStatus.toString()), SesameUtils.operationsGraph());
		/*Optional*/
		SesameUtils.addTripleString(familyURI, SKOS.PREF_LABEL, family.getPrefLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(familyURI, DCTERMS.ABSTRACT, family.getAbstractLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(familyURI, DCTERMS.ABSTRACT, family.getAbstractLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		RepositoryGestion.keepHierarchicalOperationLinks(familyURI,model);
		
		RepositoryGestion.loadSimpleObject(familyURI, model, null);
	}


	public String createFamily(String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Family family = new Family();
		String id = family.getId();
		try {
			family = mapper.readValue(body,Family.class);
			family.id = id;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfFamily(family,INSEE.UNPUBLISHED);
		logger.info("Create family : " + id + " - " + family.getPrefLabelLg1());
		return id;

	}


	public String setFamilyValidation(String id) throws RmesUnauthorizedException, RmesException  {
		Model model = new LinkedHashModel();
		
		//TODO Check autorisation
			FamilyPublication.publishFamily(id);
		
			URI familyURI = SesameUtils.objectIRI(ObjectType.FAMILY, id);
			model.add(familyURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.VALIDATED), SesameUtils.operationsGraph());
			model.remove(familyURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.UNPUBLISHED), SesameUtils.operationsGraph());
			model.remove(familyURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.MODIFIED), SesameUtils.operationsGraph());
			logger.info("Validate family : " + familyURI);

			RepositoryGestion.objectsValidation(familyURI, model);
			
		return id;
	}
	
	private void setFamilyValidationStatus(URI familyURI, String status) throws RmesException{
		RepositoryGestion.executeUpdate(FamOpeSerQueries.setPublicationState(familyURI,status));
	}
	
}
