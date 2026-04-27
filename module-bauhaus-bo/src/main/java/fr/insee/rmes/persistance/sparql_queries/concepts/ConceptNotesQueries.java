package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.DatableNote;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConceptNotesQueries {

	private static final String NOTES_FOLDER = "concepts/notes/";
	private static final String CONCEPT_ID = "CONCEPT_ID";

	private final Config config;

	public ConceptNotesQueries(Config config) {
		this.config = config;
	}

	private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest(NOTES_FOLDER, fileName, params);
	}

	public String getLastVersionnableNoteVersion(String conceptId, IRI predicat) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(CONCEPT_ID, conceptId);
		params.put("PREDICAT", predicat);
		return buildRequest("getLastVersionnableNoteVersion.ftlh", params);
	}

	public String getConceptVersion(String conceptId) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(CONCEPT_ID, conceptId);
		return buildRequest("getConceptVersion.ftlh", params);
	}

	public String getChangeNoteToDelete(String conceptId, DatableNote datableNote) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(CONCEPT_ID, conceptId);
		params.put("LANG", datableNote.getLang());
		params.put("CONCEPT_VERSION", datableNote.getConceptVersion());
		return buildRequest("getChangeNoteToDelete.ftlh", params);
	}

	public String getHistoricalNotes(String conceptId, String maxVersion) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(CONCEPT_ID, conceptId);
		params.put("MAX_VERSION", maxVersion);
		params.put("CONCEPTS_GRAPH", config.getConceptsGraph());
		return buildRequest("getHistoricalNotes.ftlh", params);
	}

	public String isExist(IRI note) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("NOTE", note);
		return buildRequest("isNoteExist.ftlh", params);
	}

	public String isClosed(IRI note) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("NOTE", note);
		return buildRequest("isNoteClosed.ftlh", params);
	}
}
