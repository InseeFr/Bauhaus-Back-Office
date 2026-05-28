package fr.insee.rmes.bauhaus_services.concepts.concepts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.Constants;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.notes.NoteManager;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.domain.model.Language;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.concepts.Concept;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionNotFoundException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.JSONUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConceptsUtils extends RdfService {
    private final BauhausLanguagesProperties languages;


	private static final Logger logger = LoggerFactory.getLogger(ConceptsUtils.class);
	private final ConceptsPublication conceptsPublication;
	private final NoteManager noteManager;
	private final int maxLength;
	private final ConceptConceptsQueries conceptConceptsQueries;
	private final ConceptsService conceptsService;
	private final CollectionsService collectionsService;

	public ConceptsUtils(RepositoryGestion repoGestion, IdGenerator idGenerator,
						 RepositoryPublication repositoryPublication, BauhausLanguagesProperties languages,
						 PublicationUtils publicationUtils,
						 ConceptsPublication conceptsPublication, NoteManager noteManager, @Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength, ConceptConceptsQueries conceptConceptsQueries, ConceptsService conceptsService, CollectionsService collectionsService) {
		super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.languages = languages;
		this.conceptsPublication = conceptsPublication;
		this.noteManager = noteManager;
		this.maxLength = maxLength;
		this.conceptConceptsQueries = conceptConceptsQueries;
		this.conceptsService = conceptsService;
		this.collectionsService = collectionsService;
	}

	public String getConceptExportFileName(ConceptForExport concept) {
		return getAbstractExportFileName(concept.getId(), concept.getPrefLabelLg1(), concept.getPrefLabelLg2(), Language.lg1);
	}

	private String getAbstractExportFileName(String id, String labelLg1, String labelLg2, Language lg){
		var initialFileName = getInitialFileName(labelLg1, labelLg2, lg);
		return FilesUtils.generateFinalFileNameWithoutExtension(id + "-" + initialFileName, maxLength);
	}

	private String getInitialFileName(String labelLg1, String labelLg2, Language lg){
		if(lg == Language.lg2){
			return labelLg2;
		}
		return labelLg1;
	}

	public String createID() throws RmesException {
		JSONObject json = repoGestion.getResponseAsObject(conceptConceptsQueries.lastConceptID());
		if (json != null && !json.isEmpty()) {
			String notation = json.getString(Constants.NOTATION);
			int id = Integer.parseInt(notation.substring(1))+1;
			return "c" + id;
		}
		return "c0001";
	}

	public JSONObject getConceptById(String id)  throws RmesException{
		if (!checkIfConceptExists(id)) {
			throw new RmesNotFoundException(ErrorCodes.CONCEPT_UNKNOWN_ID,"This concept cannot be found in database: ", id);
		}
		JSONObject concept = repoGestion.getResponseAsObject(conceptConceptsQueries.conceptQuery(id));
		JSONArray altLabelLg1 = repoGestion.getResponseAsArray(conceptConceptsQueries.altLabel(id, languages.lg1()));
		JSONArray altLabelLg2 = repoGestion.getResponseAsArray(conceptConceptsQueries.altLabel(id, languages.lg2()));
		if(!altLabelLg1.isEmpty()) {
			concept.put(Constants.ALT_LABEL_LG1, JSONUtils.extractFieldToArray(altLabelLg1, "altLabel"));
		}
		if(!altLabelLg2.isEmpty()) {
			concept.put(Constants.ALT_LABEL_LG2, JSONUtils.extractFieldToArray(altLabelLg2, "altLabel"));
		}
		try {
			List<String> collections = conceptsService.getCollectionIdsByConceptId(id);
			concept.put("collections", new JSONArray(collections));
		} catch (ConceptsFetchException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Error fetching concept collections");
		}
		return concept;
	}

	/**
	 * CREATION
	 * @param body
	 * @return
	 * @throws RmesException
	 */
	public String setConcept(String body) throws RmesException {
		Concept concept = setConcept(createID(), true, body);
		logger.info("Create concept : {} - {}", concept.getId() , concept.getPrefLabelLg1());
		return concept.getId();
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @throws RmesException
	 */
	public void setConcept(String id, String body) throws RmesException {
		Concept concept = setConcept(id, false, body);
		logger.info("Update concept : {} - {}" , concept.getId() , concept.getPrefLabelLg1());
	}
	
	private Concept setConcept(String id, boolean isNewConcept, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Concept concept = new Concept(id, isNewConcept);
		try {
			concept =  mapper.readerForUpdating(concept).readValue(body);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "IOException");
		}
		if (!isNewConcept && concept.getCreated() == null) {
			JSONObject existing = repoGestion.getResponseAsObject(conceptConceptsQueries.getConceptCreated(id));
			if (existing != null && existing.has("created")) {
				concept.setCreated(existing.getString("created"));
			}
		}
		if (concept.getCollections() != null) {
			try {
				collectionsService.validateCollections(concept.getCollections());
			} catch (CollectionsFetchException e) {
				Throwable cause = e.getCause();
				if (cause instanceof CollectionNotFoundException notFound) {
					throw new RmesBadRequestException(notFound.getMessage(), "Collection not found");
				}
				throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Error validating collections");
			}
		}
		createRdfConcept(concept);
		if (concept.getCollections() != null) {
			try {
				collectionsService.syncConceptCollections(id, concept.getCollections());
			} catch (CollectionsSaveException | CollectionsFetchException e) {
				throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Error syncing concept collections");
			}
		}
		return concept;
	}

	public void conceptsValidation(String body) throws RmesException  {
		JSONArray conceptsToValidate = new JSONArray(body);
		conceptsValidation(conceptsToValidate);
	}

	/**
	 * Concepts to rdf
	 * @throws RmesException 
	 */

	public void createRdfConcept(Concept concept) throws RmesException {
		Model model = new LinkedHashModel();
		IRI conceptURI = RdfUtils.conceptIRI(concept.getId());
		/*Const*/
		model.add(conceptURI, RDF.TYPE, SKOS.CONCEPT, RdfUtils.conceptGraph());
		model.add(conceptURI, SKOS.IN_SCHEME, RdfUtils.conceptScheme(), RdfUtils.conceptGraph());
		model.add(conceptURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(validationStateForWrite(concept)), RdfUtils.conceptGraph());
		/*Required*/
		model.add(conceptURI, SKOS.NOTATION, RdfUtils.setLiteralString(concept.getId()), RdfUtils.conceptGraph());
		model.add(conceptURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(concept.getPrefLabelLg1(), languages.lg1()), RdfUtils.conceptGraph());

		RdfUtils.addTripleUri(conceptURI, DC.CREATOR, concept.getCreator(), model, RdfUtils.conceptGraph());
		RdfUtils.addTripleUri(conceptURI, DC.CONTRIBUTOR, concept.getContributor(), model, RdfUtils.conceptGraph());

		model.add(conceptURI, INSEE.DISSEMINATIONSTATUS, RdfUtils.toURI(concept.getDisseminationStatus()), RdfUtils.conceptGraph());
		RdfUtils.addTripleDateTime(conceptURI, DCTERMS.CREATED, concept.getCreated(), model, RdfUtils.conceptGraph());
		/*Optional*/
		RdfUtils.addTripleString(conceptURI, SKOS.PREF_LABEL, concept.getPrefLabelLg2(), languages.lg2(), model, RdfUtils.conceptGraph());
		List<String> altLabelsLg1 = concept.getAltLabelLg1();
		List<String> altLabelsLg2 =  concept.getAltLabelLg2();
		if (altLabelsLg1!=null) {
			for (String altLabelLg1 : altLabelsLg1) {
				RdfUtils.addTripleString(conceptURI, SKOS.ALT_LABEL, altLabelLg1, languages.lg1(), model, RdfUtils.conceptGraph());
			}
		}
		if (altLabelsLg2!=null) {
			for (String altLabelLg2 : altLabelsLg2) {
				RdfUtils.addTripleString(conceptURI, SKOS.ALT_LABEL, altLabelLg2, languages.lg2(), model, RdfUtils.conceptGraph());
			}		
		}
		RdfUtils.addTripleString(conceptURI, INSEE.ADDITIONALMATERIAL, concept.getAdditionalMaterial(), model, RdfUtils.conceptGraph());
		RdfUtils.addTripleDateTime(conceptURI, DCTERMS.VALID, concept.getValid(), model, RdfUtils.conceptGraph());
		RdfUtils.addTripleDateTime(conceptURI, DCTERMS.MODIFIED, concept.getModified(), model, RdfUtils.conceptGraph());

		// Add notes to model, delete some notes and updates some other notes
		List<List<IRI>> notesToDeleteAndUpdate = noteManager.setNotes(concept, model);

		// Add links to model and save member links
		new LinksUtils().createRdfLinks(conceptURI, concept.getLinks(), model);

		repoGestion.loadConcept(conceptURI, model, notesToDeleteAndUpdate);
	}

	private void conceptsValidation(JSONArray conceptsToValidate) throws RmesException  {
		Model model = new LinkedHashModel();
		List<IRI> conceptsToValidateList = new ArrayList<>();
		for (int i = 0; i < conceptsToValidate.length(); i++) {
			IRI conceptURI = RdfUtils.conceptIRI(conceptsToValidate.getString(i));
			conceptsToValidateList.add(conceptURI);
			model.add(conceptURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.conceptGraph());
			logger.info("Validate concept : {}" , conceptURI);
		}
		repoGestion.objectsValidation(conceptsToValidateList, model);
		conceptsPublication.publishConcepts(conceptsToValidate);
	}

	private ValidationStatus validationStateForWrite(Concept concept) throws RmesException {
		if (Boolean.TRUE.equals(concept.getCreation())) {
			return ValidationStatus.UNPUBLISHED;
		}
		String current = getCurrentValidationStatus(concept.getId());
		if (ValidationStatus.VALIDATED.getValue().equals(current) || ValidationStatus.MODIFIED.getValue().equals(current)) {
			return ValidationStatus.MODIFIED;
		}
		return ValidationStatus.UNPUBLISHED;
	}

	private String getCurrentValidationStatus(String id) throws RmesException {
		try {
			JSONObject response = repoGestion.getResponseAsObject(conceptConceptsQueries.getConceptValidationStatus(id));
			if (response != null && response.has("state")) {
				return response.getString("state");
			}
		} catch (JSONException e) {
			logger.debug("No current validation status for concept {}", id);
		}
		return ValidationStatus.UNPUBLISHED.getValue();
	}

	public JSONArray getGraphsWithConcept(String id) throws RmesException {
		return repoGestion.getResponseAsArray(conceptConceptsQueries.getGraphWithConceptQuery(id));
	}

	public JSONArray getRelatedConcepts(String id)  throws RmesException{
		return repoGestion.getResponseAsArray(conceptConceptsQueries.getRelatedConceptsQuery(id));
	}

	public HttpStatus deleteConcept(String id) throws RmesException{
		HttpStatus result =  repoGestion.executeUpdate(conceptConceptsQueries.deleteConcept(RdfUtils.toString(RdfUtils.objectIRI(ObjectType.CONCEPT,id)),RdfUtils.conceptGraph().toString()));
		if (result.equals(HttpStatus.OK)) {
			result = repositoryPublication.executeUpdate(conceptConceptsQueries.deleteConcept(RdfUtils.toString(RdfUtils.objectIRIPublication(ObjectType.CONCEPT,id)),RdfUtils.conceptGraph().toString()));
		}
		return result;
	}

	public boolean checkIfConceptExists(String id) throws RmesException {
		return repoGestion.getResponseAsBoolean(conceptConceptsQueries.checkIfExists(id));
	}


}
