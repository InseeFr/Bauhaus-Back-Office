package fr.insee.rmes.persistance.service.sesame.concepts.concepts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.service.sesame.concepts.publication.ConceptsPublication;
import fr.insee.rmes.persistance.service.sesame.links.LinksUtils;
import fr.insee.rmes.persistance.service.sesame.notes.NoteManager;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;
import fr.insee.rmes.utils.JSONUtils;

@Component
public class ConceptsUtils {
	
	static final Logger logger = LogManager.getLogger(ConceptsUtils.class);
	
	@Autowired
	StampsRestrictionsService stampsRestrictionsService;
	
	/**
	 * Concepts
	 */
	
	public String createID()  throws RmesException{
		JSONObject json = RepositoryGestion.getResponseAsObject(ConceptsQueries.lastConceptID());
		String notation = json.getString("notation");
		int id = Integer.parseInt(notation.substring(1))+1;
		return "c" + id;
	}
	
	public JSONObject getConceptById(String id)  throws RmesException{
		JSONObject concept = RepositoryGestion.getResponseAsObject(ConceptsQueries.conceptQuery(id));
		JSONArray altLabelLg1 = RepositoryGestion.getResponseAsArray(ConceptsQueries.altLabel(id, Config.LG1));
		JSONArray altLabelLg2 = RepositoryGestion.getResponseAsArray(ConceptsQueries.altLabel(id, Config.LG2));
		if(altLabelLg1.length() != 0) concept.put("altLabelLg1", JSONUtils.extractFieldToArray(altLabelLg1, "altLabel"));
		if(altLabelLg2.length() != 0) concept.put("altLabelLg2", JSONUtils.extractFieldToArray(altLabelLg2, "altLabel"));
		return concept;
	}
	
	public String setConcept(String body) throws RmesException {
				
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Concept concept = new Concept();
		try {
			concept = mapper.readValue(body, Concept.class);
		} catch (IOException e) {
			throw new RmesException(500, e.getMessage(), "IOException");
		}
		createRdfConcept(concept);
		logger.info("Create concept : " + concept.getId() + " - " + concept.getPrefLabelLg1());
		return concept.getId();
	}
	
	public void setConcept(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Concept concept = new Concept(id);
		try {
			concept = mapper.readerForUpdating(concept).readValue(body);
		} catch (IOException e) {
			throw new RmesException(500, e.getMessage(), "IOException");
		}
		createRdfConcept(concept);
		logger.info("Update concept : " + concept.getId() + " - " + concept.getPrefLabelLg1());
	}
	
	public void conceptsValidation(String body) throws RmesUnauthorizedException, RmesException  {
		JSONArray conceptsToValidate = new JSONArray(body);
		conceptsValidation(conceptsToValidate);
	}
	
	/**
	 * Concepts to sesame
	 * @throws RmesException 
	 */
		
	public void createRdfConcept(Concept concept) throws RmesException {
		Model model = new LinkedHashModel();
		URI conceptURI = SesameUtils.conceptIRI(concept.getId());
		/*Const*/
		model.add(conceptURI, RDF.TYPE, SKOS.CONCEPT, SesameUtils.conceptGraph());
		model.add(conceptURI, SKOS.IN_SCHEME, SesameUtils.conceptScheme(), SesameUtils.conceptGraph());
		model.add(conceptURI, INSEE.IS_VALIDATED, SesameUtils.setLiteralBoolean(false), SesameUtils.conceptGraph());
		/*Required*/
		model.add(conceptURI, SKOS.NOTATION, SesameUtils.setLiteralString(concept.getId()), SesameUtils.conceptGraph());
		model.add(conceptURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(concept.getPrefLabelLg1(), Config.LG1), SesameUtils.conceptGraph());
		model.add(conceptURI, DC.CREATOR, SesameUtils.setLiteralString(concept.getCreator()), SesameUtils.conceptGraph());
		model.add(conceptURI, DC.CONTRIBUTOR, SesameUtils.setLiteralString(concept.getContributor()), SesameUtils.conceptGraph());
		model.add(conceptURI, INSEE.DISSEMINATIONSTATUS, SesameUtils.toURI(concept.getDisseminationStatus()), SesameUtils.conceptGraph());
		model.add(conceptURI, DCTERMS.CREATED, SesameUtils.setLiteralDateTime(concept.getCreated()), SesameUtils.conceptGraph());
		/*Optional*/
		SesameUtils.addTripleString(conceptURI, SKOS.PREF_LABEL, concept.getPrefLabelLg2(), Config.LG2, model, SesameUtils.conceptGraph());
		List<String> altLabelsLg1 = concept.getAltLabelLg1();
		List<String> altLabelsLg2 = concept.getAltLabelLg2();
		for (String altLabelLg1 : altLabelsLg1) {
			SesameUtils.addTripleString(conceptURI, SKOS.ALT_LABEL, altLabelLg1, Config.LG1, model, SesameUtils.conceptGraph());
		}
		for (String altLabelLg2 : altLabelsLg2) {
			SesameUtils.addTripleString(conceptURI, SKOS.ALT_LABEL, altLabelLg2, Config.LG2, model, SesameUtils.conceptGraph());
		}		
		SesameUtils.addTripleString(conceptURI, INSEE.ADDITIONALMATERIAL, concept.getAdditionalMaterial(), model, SesameUtils.conceptGraph());
		SesameUtils.addTripleDateTime(conceptURI, DCTERMS.VALID, concept.getValid(), model, SesameUtils.conceptGraph());
		SesameUtils.addTripleDateTime(conceptURI, DCTERMS.MODIFIED, concept.getModified(), model, SesameUtils.conceptGraph());
		
		// Add notes to model, delete some notes and updates some other notes
		List<List<URI>> notesToDeleteAndUpdate = new NoteManager().setNotes(concept, model);

		// Add links to model and save member links
		new LinksUtils().createRdfLinks(conceptURI, concept.getLinks(), model);
		
		RepositoryGestion.loadConcept(conceptURI, model, notesToDeleteAndUpdate);
	}
	
	public void conceptsValidation(JSONArray conceptsToValidate) throws RmesUnauthorizedException, RmesException  {
		Model model = new LinkedHashModel();
		List<URI> conceptsToValidateList = new ArrayList<>();
		for (int i = 0; i < conceptsToValidate.length(); i++) {
			URI conceptURI = SesameUtils.conceptIRI(conceptsToValidate.getString(i));
			conceptsToValidateList.add(conceptURI);
			model.add(conceptURI, INSEE.IS_VALIDATED, SesameUtils.setLiteralBoolean(true), SesameUtils.conceptGraph());
			logger.info("Validate concept : " + conceptURI);
		}
		if (!stampsRestrictionsService.isConceptsOrCollectionsOwner(conceptsToValidateList))
			throw new RmesUnauthorizedException();
		RepositoryGestion.objectsValidation(conceptsToValidateList, model);
		ConceptsPublication.publishConcepts(conceptsToValidate);
	}

}
