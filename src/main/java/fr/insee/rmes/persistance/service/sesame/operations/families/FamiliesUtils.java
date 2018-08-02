package fr.insee.rmes.persistance.service.sesame.operations.families;

import java.io.IOException;

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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class FamiliesUtils {

	final static Logger logger = LogManager.getLogger(FamiliesUtils.class);

/*READ*/
	public JSONObject getFamilyById(String id){
		JSONObject family = RepositoryGestion.getResponseAsObject(FamiliesQueries.familyQuery(id));
		addFamilySeries(id, family);
		addSubjects(id, family);
		return family;
	}


	private void addFamilySeries(String idFamily, JSONObject family) {
		JSONArray series = RepositoryGestion.getResponseAsArray(FamiliesQueries.getSeries(idFamily));
		if (series.length() != 0) {
			family.put("series", series);
		}
	}

	private void addSubjects(String idFamily, JSONObject family) {
		JSONArray subjects = RepositoryGestion.getResponseAsArray(FamiliesQueries.getSubjects(idFamily));
		if (subjects.length() != 0) {
			family.put("subjects", subjects);
		}
	}


/*WRITE*/
	public void setFamily(String id, String body) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Family family = new Family(id);
		try {
			family = mapper.readerForUpdating(family).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfFamily(family);
		logger.info("Update family : " + family.getId() + " - " + family.getPrefLabelLg1());
		
	}
	
	
	/**
	 * Concepts to sesame
	 */
		
	public void createRdfFamily(Family family) {
		Model model = new LinkedHashModel();
		URI familyURI = SesameUtils.objectIRI(ObjectType.FAMILY,family.getId());
		/*Const*/
		model.add(familyURI, RDF.TYPE, INSEE.FAMILY, SesameUtils.operationsGraph());
		/*Required*/
		model.add(familyURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(family.getPrefLabelLg1(), Config.LG1), SesameUtils.operationsGraph());
		/*Optional*/
		SesameUtils.addTripleString(familyURI, SKOS.PREF_LABEL, family.getPrefLabelLg2(), Config.LG2, model);
		SesameUtils.addTripleString(familyURI, DCTERMS.ABSTRACT, family.getAbstractLg1(), Config.LG1, model);
		SesameUtils.addTripleString(familyURI, DCTERMS.ABSTRACT, family.getAbstractLg2(), Config.LG2, model);

		RepositoryGestion.keepHierarchicalOperationLinks(familyURI,model);
		
		RepositoryGestion.loadFamily(familyURI, model);
	}
	
}
