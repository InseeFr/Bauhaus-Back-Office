package fr.insee.rmes.persistance.service.sesame.concepts.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.modele.concepts.Collection;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.concepts.publication.ConceptsPublication;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameService;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class CollectionsUtils extends SesameService{
	
	static final Logger logger = LogManager.getLogger(CollectionsUtils.class);
	
	/**
	 * Collections
	 * @throws RmesException 
	 */
	
	public void setCollection(String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Collection collection = new Collection();
		try {
			collection = mapper.readValue(body, Collection.class);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
		setRdfCollection(collection);
		logger.info("Create collection : {} - {} ",  collection.getId(), collection.getPrefLabelLg1());
	}
	
	public void setCollection(String id, String body) throws RmesException  {
		URI collectionURI = SesameUtils.collectionIRI(id);
		ObjectMapper mapper = new ObjectMapper();
		if (!stampsRestrictionsService.isConceptOrCollectionOwner(collectionURI)) {
			throw new RmesUnauthorizedException(ErrorCodes.COLLECTION_MODIFICATION_RIGHTS_DENIED,"rights denied",id);
		}
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Collection collection = new Collection(id);
		try {
			collection = mapper.readerForUpdating(collection).readValue(body);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");

		}
		setRdfCollection(collection);
		logger.info("Update collection : {} - {} ",  collection.getId(), collection.getPrefLabelLg1());
	}
	
	public void collectionsValidation(String body) throws RmesException   {
		JSONArray collectionsToValidate = new JSONArray(body);
		collectionsValidation(collectionsToValidate);
	}
	
	/**
	 * Collections to sesame
	 * @throws RmesException 
	 */
	
	public void setRdfCollection(Collection collection) throws RmesException {
		Model model = new LinkedHashModel();
		URI collectionURI = SesameUtils.collectionIRI(collection.getId().replace(" ", "-").toLowerCase());
		/*Required*/
		model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, SesameUtils.conceptGraph());	
		model.add(collectionURI, INSEE.IS_VALIDATED, SesameUtils.setLiteralBoolean(collection.getIsValidated()), SesameUtils.conceptGraph());
		model.add(collectionURI, DCTERMS.TITLE, SesameUtils.setLiteralString(collection.getPrefLabelLg1(), Config.LG1), SesameUtils.conceptGraph());
		model.add(collectionURI, DCTERMS.CREATED, SesameUtils.setLiteralDateTime(collection.getCreated()), SesameUtils.conceptGraph());	
		model.add(collectionURI, DC.CONTRIBUTOR, SesameUtils.setLiteralString(collection.getContributor()), SesameUtils.conceptGraph());
		model.add(collectionURI, DC.CREATOR, SesameUtils.setLiteralString(collection.getCreator()), SesameUtils.conceptGraph());
		/*Optional*/
		SesameUtils.addTripleDateTime(collectionURI, DCTERMS.MODIFIED, collection.getModified(), model, SesameUtils.conceptGraph());
		SesameUtils.addTripleString(collectionURI, DCTERMS.TITLE, collection.getPrefLabelLg2(), Config.LG2, model, SesameUtils.conceptGraph());
		SesameUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg1(), Config.LG1, model, SesameUtils.conceptGraph());
		SesameUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg2(), Config.LG2, model, SesameUtils.conceptGraph());
		
		/*Members*/
		collection.getMembers().forEach(member->{
			URI memberIRI = SesameUtils.conceptIRI(member);
		    model.add(collectionURI, SKOS.MEMBER, memberIRI, SesameUtils.conceptGraph());
		});
		
		repoGestion.loadSimpleObject(collectionURI, model, null);
	}
	
	public void collectionsValidation(JSONArray collectionsToValidate) throws  RmesException  {
		Model model = new LinkedHashModel();
		List<URI> collectionsToValidateList = new ArrayList<>();
		for (int i = 0; i < collectionsToValidate.length(); i++) {
			URI collectionURI = SesameUtils.collectionIRI(collectionsToValidate.getString(i).replace(" ", "").toLowerCase());
			collectionsToValidateList.add(collectionURI);
			model.add(collectionURI, INSEE.IS_VALIDATED, SesameUtils.setLiteralBoolean(true), SesameUtils.conceptGraph());
			logger.info("Validate collection : {}" , collectionURI);
		}
		if (!stampsRestrictionsService.isConceptsOrCollectionsOwner(collectionsToValidateList)) {
			throw new RmesUnauthorizedException(ErrorCodes.CONCEPT_VALIDATION_RIGHTS_DENIED,collectionsToValidate);
		}
		RepositoryGestion.objectsValidation(collectionsToValidateList, model);
		ConceptsPublication.publishCollection(collectionsToValidate);
	}

}
