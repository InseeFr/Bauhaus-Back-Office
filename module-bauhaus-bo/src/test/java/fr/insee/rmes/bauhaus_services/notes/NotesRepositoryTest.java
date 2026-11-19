package fr.insee.rmes.bauhaus_services.notes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.config.BauhausIriFactoryStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.EVOC;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.XKOS;
import fr.insee.rmes.model.concepts.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.DatableNote;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.VersionableNote;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptNotesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.io.IOException;
import java.time.Instant;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class NotesRepositoryTest {

    private static final BauhausIriFactory IRI_FACTORY = BauhausIriFactoryStub.stub("concepts/definition");

    @Test
    void shouldGetVersion() throws RmesException {
        Concept concept = new Concept("id", true);
        VersionableNote versionableNote = new VersionableNote();
        String defaultBVersion = "defaultVersion";
        NotesRepository notesRepository = new NotesRepository(null, null, null, null, null, IRI_FACTORY);
        String result = notesRepository.getVersion(concept, versionableNote, defaultBVersion);
        assertEquals("1", result);
    }

    @Test
    void createRdfDatableNote_shouldAddExpectedStatements() {
        NotesRepository notesRepository = new NotesRepository(null, null, null, null, null, IRI_FACTORY);

        ValueFactory vf = SimpleValueFactory.getInstance();

        String conceptId = "C123";

        DatableNote noteDto = mock(DatableNote.class);
        IRI predicate = vf.createIRI("http://example.org/predicate/explanatoryNote");
        when(noteDto.getPredicat()).thenReturn(predicate);
        when(noteDto.getLang()).thenReturn("fr");
        when(noteDto.getConceptVersion()).thenReturn("42");
        Instant issued = Instant.parse("2024-03-01T10:15:30Z");
        when(noteDto.getIssued()).thenReturn(issued.toString());
        String content = "<p>Texte riche</p>";
        when(noteDto.getContent()).thenReturn(content);

        Model model = mock(Model.class);

        IRI conceptIri = vf.createIRI("http://bauhaus/concepts/definition/C123");
        IRI noteIri = IRI_FACTORY.datableNote(conceptId, noteDto);
        IRI graph = vf.createIRI("http://rdf.insee.fr/graphes/concepts/");

        Literal langLit = RdfTriples.language("fr");
        Literal versionLit = RdfTriples.integer("42");
        Literal issuedLit = RdfTriples.dateTime(issued.toString());
        Literal xmlLit = RdfTriples.xmlLiteral(content);
        Literal stringLit = RdfTriples.string(content);

        notesRepository.createRdfDatableNote(conceptId, noteDto, model);

        verify(model).add(conceptIri, predicate, noteIri, graph);
        verify(model).add(noteIri, RDF.TYPE, XKOS.EXPLANATORY_NOTE, graph);
        verify(model).add(noteIri, DCTERMS.LANGUAGE, langLit, graph);
        verify(model).add(noteIri, INSEE.CONCEPT_VERSION, versionLit, graph);
        verify(model).add(noteIri, DCTERMS.ISSUED, issuedLit, graph);
        verify(model).add(noteIri, EVOC.NOTE_LITERAL, xmlLit, graph);
        verify(model).add(noteIri, RDF.VALUE, stringLit, graph);
        verifyNoMoreInteractions(model);
    }

    @Test
    void shouldStartAtVersionOne_whenTheNoteHasNeverBeenFilledIn_andTheConceptIsVersioned() throws Exception {
        NotesRepository notesRepository = notesRepositoryReturning(new JSONObject());

        String version = notesRepository.getVersion(conceptUnderUpdate(true), new VersionableNote(), "1");

        assertEquals("1", version);
    }

    @Test
    void shouldIncrementTheLastVersion_whenTheNoteAlreadyExists_andTheConceptIsVersioned() throws Exception {
        NotesRepository notesRepository = notesRepositoryReturning(new JSONObject().put("version", "3"));

        String version = notesRepository.getVersion(conceptUnderUpdate(true), new VersionableNote(), "1");

        assertEquals("4", version);
    }

    @Test
    void shouldKeepTheCurrentVersion_whenTheConceptIsNotVersioned() throws Exception {
        NotesRepository notesRepository = notesRepositoryReturning(new JSONObject().put("version", "3"));

        String version = notesRepository.getVersion(conceptUnderUpdate(false), new VersionableNote(), "1");

        assertEquals("3", version);
    }

    private static NotesRepository notesRepositoryReturning(JSONObject lastVersionResponse) throws RmesException {
        RepositoryGestion repoGestion = mock(RepositoryGestion.class);
        when(repoGestion.getResponseAsObject(any())).thenReturn(lastVersionResponse);
        return new NotesRepository(repoGestion, null, null, null, mock(ConceptNotesQueries.class), IRI_FACTORY);
    }

    private static Concept conceptUnderUpdate(boolean versioning) throws IOException {
        return new ObjectMapper()
                .readerForUpdating(new Concept("c1000", false))
                .readValue("{\"versioning\":" + versioning + "}");
    }
}
