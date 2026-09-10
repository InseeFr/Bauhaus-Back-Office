package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.DatableNote;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

@Component
public class ConceptNotesQueries {

    private static final String NOTES_FOLDER = "concepts/notes/";
    private static final String CONCEPT_ID = "CONCEPT_ID";
    private static final String CONCEPT_URI_PATTERN = "CONCEPT_URI_PATTERN";
    private static final String CONCEPT_PATH = "/concepts/definition/";

    private final GraphsProperties graphs;

    public ConceptNotesQueries(GraphsProperties graphs) {
        this.graphs = graphs;
    }

    private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest(NOTES_FOLDER, fileName, params);
    }

    public String getLastVersionnableNoteVersion(String conceptId, IRI predicat) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(CONCEPT_URI_PATTERN, SparqlLiterals.literal(CONCEPT_PATH + conceptId));
        params.put("PREDICAT", SparqlLiterals.iri(predicat.stringValue()));
        return buildRequest("getLastVersionnableNoteVersion.ftlh", params);
    }

    public String getConceptVersion(String conceptId) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(CONCEPT_URI_PATTERN, SparqlLiterals.literal(CONCEPT_PATH + conceptId));
        return buildRequest("getConceptVersion.ftlh", params);
    }

    public String getChangeNoteToDelete(String conceptId, DatableNote datableNote) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(CONCEPT_URI_PATTERN, SparqlLiterals.literal(CONCEPT_PATH + conceptId));
        params.put("LANG", SparqlLiterals.literal(datableNote.getLang()));
        params.put("CONCEPT_VERSION", SparqlLiterals.literal(String.valueOf(datableNote.getConceptVersion())));
        return buildRequest("getChangeNoteToDelete.ftlh", params);
    }

    public String getHistoricalNotes(String conceptId, String maxVersion) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(CONCEPT_URI_PATTERN, SparqlLiterals.literal(CONCEPT_PATH + conceptId));
        params.put("MAX_VERSION", maxVersion);
        params.put("CONCEPTS_GRAPH", SparqlLiterals.iri(graphs.conceptsGraph()));
        return buildRequest("getHistoricalNotes.ftlh", params);
    }

    public String isExist(IRI note) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("NOTE", SparqlLiterals.iri(note.stringValue()));
        return buildRequest("isNoteExist.ftlh", params);
    }

    public String isClosed(IRI note) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("NOTE", SparqlLiterals.iri(note.stringValue()));
        return buildRequest("isNoteClosed.ftlh", params);
    }
}
