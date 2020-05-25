package fr.insee.rmes.bauhaus_services.concepts.concepts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.links.LinksUtils;
import fr.insee.rmes.bauhaus_services.notes.NoteManager;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.concepts.Concept;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.utils.JSONUtils;

@Component
public class ConceptsUtils  extends RdfService {

	static final Logger logger = LogManager.getLogger(ConceptsUtils.class);
	
	@Autowired
	ConceptsPublication conceptsPublication;

	/**
	 * Concepts
	 */

	public String createID()  throws RmesException{
		JSONObject json = repoGestion.getResponseAsObject(ConceptsQueries.lastConceptID());
		String notation = json.getString("notation");
		int id = Integer.parseInt(notation.substring(1))+1;
		return "c" + id;
	}

	public JSONObject getConceptById(String id)  throws RmesException{
		if (!checkIfConceptExists(id)) {
			throw new RmesNotFoundException(ErrorCodes.CONCEPT_UNKNOWN_ID,"This concept cannot be found in database: ", id);
		}
		JSONObject concept = repoGestion.getResponseAsObject(ConceptsQueries.conceptQuery(id));
		JSONArray altLabelLg1 = repoGestion.getResponseAsArray(ConceptsQueries.altLabel(id, Config.LG1));
		JSONArray altLabelLg2 = repoGestion.getResponseAsArray(ConceptsQueries.altLabel(id, Config.LG2));
		if(altLabelLg1.length() != 0) {
			concept.put(Constants.ALT_LABEL_LG1, JSONUtils.extractFieldToArray(altLabelLg1, "altLabel"));
		}
		if(altLabelLg2.length() != 0) {
			concept.put(Constants.ALT_LABEL_LG2, JSONUtils.extractFieldToArray(altLabelLg2, "altLabel"));
		}
		return concept;
	}

	public String setConcept(String body) throws RmesException {
		if(!stampsRestrictionsService.canCreateConcept()) {
			throw new RmesUnauthorizedException(ErrorCodes.CONCEPT_CREATION_RIGHTS_DENIED, "Only an admin or concepts manager can create a new concept.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Concept concept = null;
		try {
			concept = mapper.readValue(body, Concept.class);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
		createRdfConcept(concept);
		logger.info("Create concept : {} - {}", concept.getId() , concept.getPrefLabelLg1());
		return concept.getId();
	}

	public void setConcept(String id, String body) throws RmesException {
		if(!stampsRestrictionsService.canModifyConcept(RdfUtils.conceptIRI(id))) {
			throw new RmesUnauthorizedException(ErrorCodes.CONCEPT_MODIFICATION_RIGHTS_DENIED, "");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Concept concept = new Concept(id);
		try {
			concept = mapper.readerForUpdating(concept).readValue(body);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
		createRdfConcept(concept);
		logger.info("Update concept : {} - {}" , concept.getId() , concept.getPrefLabelLg1());
	}

	public void conceptsValidation(String body) throws RmesException  {
		JSONArray conceptsToValidate = new JSONArray(body);
		conceptsValidation(conceptsToValidate);
	}

	/**
	 * Concepts to sesame
	 * @throws RmesException 
	 */

	public void createRdfConcept(Concept concept) throws RmesException {
		Model model = new LinkedHashModel();
		URI conceptURI = RdfUtils.conceptIRI(concept.getId());
		/*Const*/
		model.add(conceptURI, RDF.TYPE, SKOS.CONCEPT, RdfUtils.conceptGraph());
		model.add(conceptURI, SKOS.IN_SCHEME, RdfUtils.conceptScheme(), RdfUtils.conceptGraph());
		model.add(conceptURI, INSEE.IS_VALIDATED, RdfUtils.setLiteralBoolean(false), RdfUtils.conceptGraph());
		/*Required*/
		model.add(conceptURI, SKOS.NOTATION, RdfUtils.setLiteralString(concept.getId()), RdfUtils.conceptGraph());
		model.add(conceptURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(concept.getPrefLabelLg1(), Config.LG1), RdfUtils.conceptGraph());
		model.add(conceptURI, DC.CREATOR, RdfUtils.setLiteralString(concept.getCreator()), RdfUtils.conceptGraph());
		model.add(conceptURI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(concept.getContributor()), RdfUtils.conceptGraph());
		model.add(conceptURI, INSEE.DISSEMINATIONSTATUS, RdfUtils.toURI(concept.getDisseminationStatus()), RdfUtils.conceptGraph());
		model.add(conceptURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(concept.getCreated()), RdfUtils.conceptGraph());
		/*Optional*/
		RdfUtils.addTripleString(conceptURI, SKOS.PREF_LABEL, concept.getPrefLabelLg2(), Config.LG2, model, RdfUtils.conceptGraph());
		List<String> altLabelsLg1 = concept.getAltLabelLg1();
		List<String> altLabelsLg2 =  concept.getAltLabelLg2();
		if (altLabelsLg1!=null) {
			for (String altLabelLg1 : altLabelsLg1) {
				RdfUtils.addTripleString(conceptURI, SKOS.ALT_LABEL, altLabelLg1, Config.LG1, model, RdfUtils.conceptGraph());
			}
		}
		if (altLabelsLg2!=null) {
			for (String altLabelLg2 : altLabelsLg2) {
				RdfUtils.addTripleString(conceptURI, SKOS.ALT_LABEL, altLabelLg2, Config.LG2, model, RdfUtils.conceptGraph());
			}		
		}
		RdfUtils.addTripleString(conceptURI, INSEE.ADDITIONALMATERIAL, concept.getAdditionalMaterial(), model, RdfUtils.conceptGraph());
		RdfUtils.addTripleDateTime(conceptURI, DCTERMS.VALID, concept.getValid(), model, RdfUtils.conceptGraph());
		RdfUtils.addTripleDateTime(conceptURI, DCTERMS.MODIFIED, concept.getModified(), model, RdfUtils.conceptGraph());

		// Add notes to model, delete some notes and updates some other notes
		List<List<URI>> notesToDeleteAndUpdate = new NoteManager().setNotes(concept, model);

		// Add links to model and save member links
		new LinksUtils().createRdfLinks(conceptURI, concept.getLinks(), model);

		repoGestion.loadConcept(conceptURI, model, notesToDeleteAndUpdate);
	}

	public void conceptsValidation(JSONArray conceptsToValidate) throws RmesException  {
		Model model = new LinkedHashModel();
		List<URI> conceptsToValidateList = new ArrayList<>();
		for (int i = 0; i < conceptsToValidate.length(); i++) {
			URI conceptURI = RdfUtils.conceptIRI(conceptsToValidate.getString(i));
			conceptsToValidateList.add(conceptURI);
			model.add(conceptURI, INSEE.IS_VALIDATED, RdfUtils.setLiteralBoolean(true), RdfUtils.conceptGraph());
			logger.info("Validate concept : {}" , conceptURI);
		}
		if (!stampsRestrictionsService.isConceptsOrCollectionsOwner(conceptsToValidateList)) {
			throw new RmesUnauthorizedException(
					ErrorCodes.CONCEPT_VALIDATION_RIGHTS_DENIED,
					conceptsToValidate);
		}
		repoGestion.objectsValidation(conceptsToValidateList, model);
		conceptsPublication.publishConcepts(conceptsToValidate);
	}

	public JSONArray getGraphsWithConcept(String id) throws RmesException {
		return repoGestion.getResponseAsArray(ConceptsQueries.getGraphWithConceptQuery(id));
	}

	public JSONArray getRelatedConcepts(String id)  throws RmesException{
		return repoGestion.getResponseAsArray(ConceptsQueries.getRelatedConceptsQuery(id));
	}

	public Response.Status deleteConcept(String id) throws RmesException{
		Response.Status result =  repoGestion.executeUpdate(ConceptsQueries.deleteConcept(RdfUtils.objectIRI(ObjectType.CONCEPT,id).toString(),RdfUtils.conceptGraph().toString()));
		if (result.equals(Status.OK)) {
			result = RepositoryPublication.executeUpdate(ConceptsQueries.deleteConcept(RdfUtils.objectIRIPublication(ObjectType.CONCEPT,id).toString(),RdfUtils.conceptGraph().toString()));
		}
		return result;
	}

	public JSONArray getConceptVersions(String uriConcept) throws RmesException{
		return repoGestion.getResponseAsArray(ConceptsQueries.getConceptVersions(uriConcept));
	}

	public boolean checkIfConceptExists(String id) throws RmesException {
		return repoGestion.getResponseAsBoolean(ConceptsQueries.checkIfExists(id));
	}


}
