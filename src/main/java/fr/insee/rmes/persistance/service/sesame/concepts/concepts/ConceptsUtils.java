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
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.service.sesame.concepts.publication.ConceptsPublication;
import fr.insee.rmes.persistance.service.sesame.links.LinksUtils;
import fr.insee.rmes.persistance.service.sesame.notes.NoteManager;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class ConceptsUtils {
	
	final static Logger logger = LogManager.getLogger(ConceptsUtils.class);
	
	@Autowired
	StampsRestrictionsService stampsRestrictionsService;
	
	/**
	 * Concepts
	 */
	
	public String createID() {
		JSONObject json = RepositoryGestion.getResponseAsObject(ConceptsQueries.lastConceptID());
		String id = json.getString("notation");
		int ID = Integer.parseInt(id.substring(1))+1;
		return "c" + ID;
	}
	
	public String setConcept(String body) {
				
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Concept concept = new Concept();
		try {
			concept = mapper.readValue(body, Concept.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		createRdfConcept(concept);
		logger.info("Create concept : " + concept.getId() + " - " + concept.getPrefLabelLg1());
		return concept.getId();
	}
	
	public void setConcept(String id, String body) {
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Concept concept = new Concept(id);
		try {
			concept = mapper.readerForUpdating(concept).readValue(body);
		} catch (IOException e) {
			e.printStackTrace();
		}
		createRdfConcept(concept);
		logger.info("Update concept : " + concept.getId() + " - " + concept.getPrefLabelLg1());
	}
	
	public void conceptsValidation(String body) throws Exception {
		JSONArray conceptsToValidate = new JSONArray(body);
		conceptsValidation(conceptsToValidate);
	}
	
	/**
	 * Concepts to sesame
	 */
		
	public void createRdfConcept(Concept concept) {
		Model model = new LinkedHashModel();
		URI conceptURI = SesameUtils.conceptIRI(concept.getId());
		/*Const*/
		model.add(conceptURI, RDF.TYPE, SKOS.CONCEPT, SesameUtils.conceptGraph());
		model.add(conceptURI, SKOS.IN_SCHEME, SesameUtils.conceptScheme(), SesameUtils.conceptGraph());
		model.add(conceptURI, INSEE.IS_VALIDATED, SesameUtils.setLiteralString("Provisoire"), SesameUtils.conceptGraph());
		/*Required*/
		model.add(conceptURI, SKOS.NOTATION, SesameUtils.setLiteralString(concept.getId()), SesameUtils.conceptGraph());
		model.add(conceptURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(concept.getPrefLabelLg1(), Config.LG1), SesameUtils.conceptGraph());
		model.add(conceptURI, DC.CREATOR, SesameUtils.setLiteralString(concept.getCreator()), SesameUtils.conceptGraph());
		model.add(conceptURI, DC.CONTRIBUTOR, SesameUtils.setLiteralString(concept.getContributor()), SesameUtils.conceptGraph());
		model.add(conceptURI, INSEE.DISSEMINATIONSTATUS, SesameUtils.toURI(concept.getDisseminationStatus()), SesameUtils.conceptGraph());
		model.add(conceptURI, DCTERMS.CREATED, SesameUtils.setLiteralDateTime(concept.getCreated()), SesameUtils.conceptGraph());
		/*Optional*/
		SesameUtils.addTripleString(conceptURI, SKOS.PREF_LABEL, concept.getPrefLabelLg2(), Config.LG2, model);
		String arr = concept.getAltLabelLg1();
		String[] a = arr.split(" \\|\\| ");
		for (String altLabelLg1 : a) {
			SesameUtils.addTripleString(conceptURI, SKOS.ALT_LABEL, altLabelLg1, Config.LG1, model);
		}
		for (String altLabelLg2 : concept.getAltLabelLg2().split(" \\|\\| ")) {
			SesameUtils.addTripleString(conceptURI, SKOS.ALT_LABEL, altLabelLg2, Config.LG2, model);
		}		
		SesameUtils.addTripleString(conceptURI, INSEE.ADDITIONALMATERIAL, concept.getAdditionalMaterial(), model);
		SesameUtils.addTripleDateTime(conceptURI, DCTERMS.VALID, concept.getValid(), model);
		SesameUtils.addTripleDateTime(conceptURI, DCTERMS.MODIFIED, concept.getModified(), model);
		
		// Add notes to model, delete some notes and updates some other notes
		List<List<URI>> notesToDeleteAndUpdate = new NoteManager().setNotes(concept, model);

		// Add links to model and save member links
		new LinksUtils().createRdfLinks(conceptURI, concept.getLinks(), model);
		
		RepositoryGestion.loadConcept(conceptURI, model, notesToDeleteAndUpdate);
	}
	
	public void conceptsValidation(JSONArray conceptsToValidate) throws Exception {
		Model model = new LinkedHashModel();
		List<URI> conceptsToValidateList = new ArrayList<URI>();
		for (int i = 0; i < conceptsToValidate.length(); i++) {
			URI conceptURI = SesameUtils.conceptIRI(conceptsToValidate.getString(i));
			conceptsToValidateList.add(conceptURI);
			model.add(conceptURI, INSEE.IS_VALIDATED, SesameUtils.setLiteralString("Validé"), SesameUtils.conceptGraph());
			logger.info("Validate concept : " + conceptURI);
		}
		if (!stampsRestrictionsService.isConceptsOwner(conceptsToValidateList))
			throw new RmesUnauthorizedException();
		RepositoryGestion.objectsValidation(conceptsToValidateList, model);
		ConceptsPublication.publishConcepts(conceptsToValidate);
	}

}
