package fr.insee.rmes.bauhaus_services.concepts.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.persistance.ontologies.INSEE;

@Component
public class CollectionsUtils extends RdfService{
	
	static final Logger logger = LogManager.getLogger(CollectionsUtils.class);
	
	@Autowired
	ConceptsPublication conceptsPublication;
	
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
		IRI collectionURI = RdfUtils.collectionIRI(id);
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
		IRI collectionURI = RdfUtils.collectionIRI(collection.getId().replace(" ", "-").toLowerCase());
		/*Required*/
		model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, RdfUtils.conceptGraph());	
		model.add(collectionURI, INSEE.IS_VALIDATED, RdfUtils.setLiteralBoolean(collection.getIsValidated()), RdfUtils.conceptGraph());
		model.add(collectionURI, DCTERMS.TITLE, RdfUtils.setLiteralString(collection.getPrefLabelLg1(), Config.LG1), RdfUtils.conceptGraph());
		model.add(collectionURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(collection.getCreated()), RdfUtils.conceptGraph());	
		model.add(collectionURI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(collection.getContributor()), RdfUtils.conceptGraph());
		model.add(collectionURI, DC.CREATOR, RdfUtils.setLiteralString(collection.getCreator()), RdfUtils.conceptGraph());
		/*Optional*/
		RdfUtils.addTripleDateTime(collectionURI, DCTERMS.MODIFIED, collection.getModified(), model, RdfUtils.conceptGraph());
		RdfUtils.addTripleString(collectionURI, DCTERMS.TITLE, collection.getPrefLabelLg2(), Config.LG2, model, RdfUtils.conceptGraph());
		RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg1(), Config.LG1, model, RdfUtils.conceptGraph());
		RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg2(), Config.LG2, model, RdfUtils.conceptGraph());
		
		/*Members*/
		collection.getMembers().forEach(member->{
			IRI memberIRI = RdfUtils.conceptIRI(member);
		    model.add(collectionURI, SKOS.MEMBER, memberIRI, RdfUtils.conceptGraph());
		});
		
		repoGestion.loadSimpleObject(collectionURI, model, null);
	}
	
	public void collectionsValidation(JSONArray collectionsToValidate) throws  RmesException  {
		Model model = new LinkedHashModel();
		List<IRI> collectionsToValidateList = new ArrayList<>();
		for (int i = 0; i < collectionsToValidate.length(); i++) {
			IRI collectionURI = RdfUtils.collectionIRI(collectionsToValidate.getString(i).replace(" ", "").toLowerCase());
			collectionsToValidateList.add(collectionURI);
			model.add(collectionURI, INSEE.IS_VALIDATED, RdfUtils.setLiteralBoolean(true), RdfUtils.conceptGraph());
			logger.info("Validate collection : {}" , collectionURI);
		}
		if (!stampsRestrictionsService.isConceptsOrCollectionsOwner(collectionsToValidateList)) {
			throw new RmesUnauthorizedException(ErrorCodes.CONCEPT_VALIDATION_RIGHTS_DENIED,collectionsToValidate);
		}
		repoGestion.objectsValidation(collectionsToValidateList, model);
		conceptsPublication.publishCollection(collectionsToValidate);
	}

}
