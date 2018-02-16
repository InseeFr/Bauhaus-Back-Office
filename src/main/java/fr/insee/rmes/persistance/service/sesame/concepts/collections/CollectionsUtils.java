package fr.insee.rmes.persistance.service.sesame.concepts.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.sesame.concepts.collections.Collection;
import fr.insee.rmes.persistance.service.sesame.concepts.publication.ConceptsPublication;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

public class CollectionsUtils {
	
	final static Logger logger = LogManager.getLogger(CollectionsUtils.class);
	
	/**
	 * Collections
	 */
	
	public static void setCollection(String body) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Collection collection = new Collection();
		try {
			collection = mapper.readValue(body, Collection.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setRdfCollection(collection);
		logger.info("Create collection : " + collection.getId() + " - " + collection.getPrefLabelLg1());
	}
	
	public static void setCollection(String id, String body) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Collection collection = new Collection(id);
		try {
			collection = mapper.readerForUpdating(collection).readValue(body);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setRdfCollection(collection);
		logger.info("Update collection : " + collection.getId() + " - " + collection.getPrefLabelLg1());
	}
	
	public static void collectionsValidation(String body) {
		JSONArray collectionsToValidate = new JSONArray(body);
		collectionsValidation(collectionsToValidate);
	}
	
	/**
	 * Collections to sesame
	 */
	
	public static void setRdfCollection(Collection collection) {
		Model model = new LinkedHashModel();
		URI collectionURI = SesameUtils.collectionIRI(collection.getId().replaceAll(" ", "-").toLowerCase());
		/*Required*/
		model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, SesameUtils.conceptGraph());	
		model.add(collectionURI, INSEE.IS_VALIDATED, SesameUtils.setLiteralString(collection.getIsValidated()), SesameUtils.conceptGraph());
		model.add(collectionURI, DCTERMS.TITLE, SesameUtils.setLiteralString(collection.getPrefLabelLg1(), Config.LG1), SesameUtils.conceptGraph());
		model.add(collectionURI, DCTERMS.CREATED, SesameUtils.setLiteralDateTime(collection.getCreated()), SesameUtils.conceptGraph());	
		model.add(collectionURI, DC.CONTRIBUTOR, SesameUtils.setLiteralString(collection.getContributor()), SesameUtils.conceptGraph());
		model.add(collectionURI, DC.CREATOR, SesameUtils.setLiteralString(collection.getCreator()), SesameUtils.conceptGraph());
		/*Optional*/
		SesameUtils.addTripleDateTime(collectionURI, DCTERMS.MODIFIED, collection.getModified(), model);
		SesameUtils.addTripleString(collectionURI, DCTERMS.TITLE, collection.getPrefLabelLg2(), Config.LG2, model);
		SesameUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg1(), Config.LG1, model);
		SesameUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg2(), Config.LG2, model);
		
		/*Members*/
		collection.getMembers().forEach(member->{
			URI memberIRI = SesameUtils.conceptIRI(member);
		    model.add(collectionURI, SKOS.MEMBER, memberIRI, SesameUtils.conceptGraph());
		});
		
		RepositoryGestion.loadCollection(collectionURI, model);
	}
	
	public static void collectionsValidation(JSONArray collectionsToValidate) {
		Model model = new LinkedHashModel();
		List<URI> collectionsToValidateList = new ArrayList<URI>();
		for (int i = 0; i < collectionsToValidate.length(); i++) {
			URI collectionURI = SesameUtils.collectionIRI(collectionsToValidate.getString(i).replaceAll(" ", "").toLowerCase());
			collectionsToValidateList.add(collectionURI);
			model.add(collectionURI, INSEE.IS_VALIDATED, SesameUtils.setLiteralString("Validée"), SesameUtils.conceptGraph());
			logger.info("Validate collection : " + collectionURI);
		}
		RepositoryGestion.objectsValidation(collectionsToValidateList, model);
		ConceptsPublication.publishCollection(collectionsToValidate);
	}

}
