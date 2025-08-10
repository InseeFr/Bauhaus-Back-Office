package fr.insee.rmes.bauhaus_services.notes;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.Concept;
import fr.insee.rmes.model.notes.DatableNote;
import fr.insee.rmes.model.notes.VersionableNote;
import fr.insee.rmes.persistance.ontologies.EVOC;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.XKOS;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotesUtilsTest {

    @Test
    void shouldGetVersion() throws RmesException {
        Concept concept = new Concept("id",true);
        VersionableNote versionableNote = new VersionableNote();
        String defaultBVersion = "defaultVersion";
        NotesUtils notesUtils = new NotesUtils();
        String result= notesUtils.getVersion(concept,versionableNote,defaultBVersion);
        assertEquals("1",result);
    }

    @Test
    void createRdfDatableNote_shouldAddExpectedStatements() {
        NotesUtils notesUtils = new NotesUtils();


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

        IRI conceptIri = vf.createIRI("http://example.org/concept/C123");
        IRI noteIri = vf.createIRI("http://example.org/note/N1");
        IRI graph = vf.createIRI("http://example.org/graph/concepts");

        Literal langLit = vf.createLiteral("fr");
        Literal versionLit = vf.createLiteral(42);
        Literal issuedLit = vf.createLiteral(issued.toString());
        Literal xmlLit = vf.createLiteral(content);
        Literal stringLit = vf.createLiteral(content);

        try (MockedStatic<RdfUtils> rdfUtils = mockStatic(RdfUtils.class)) {
            rdfUtils.when(() -> RdfUtils.datableNoteIRI(conceptId, noteDto)).thenReturn(noteIri);
            rdfUtils.when(() -> RdfUtils.conceptIRI(conceptId)).thenReturn(conceptIri);
            rdfUtils.when(RdfUtils::conceptGraph).thenReturn(graph);

            rdfUtils.when(() -> RdfUtils.setLiteralLanguage("fr")).thenReturn(langLit);
            rdfUtils.when(() -> RdfUtils.setLiteralInt("42")).thenReturn(versionLit);
            rdfUtils.when(() -> RdfUtils.setLiteralDateTime(issued.toString())).thenReturn(issuedLit);
            rdfUtils.when(() -> RdfUtils.setLiteralXML(content)).thenReturn(xmlLit);
            rdfUtils.when(() -> RdfUtils.setLiteralString(content)).thenReturn(stringLit);

            notesUtils.createRdfDatableNote(conceptId, noteDto, model);

            verify(model).add(conceptIri, predicate, noteIri, graph);
            verify(model).add(noteIri, RDF.TYPE, XKOS.EXPLANATORY_NOTE, graph);
            verify(model).add(noteIri, DCTERMS.LANGUAGE, langLit, graph);
            verify(model).add(noteIri, INSEE.CONCEPT_VERSION, versionLit, graph);
            verify(model).add(noteIri, DCTERMS.ISSUED, issuedLit, graph);
            verify(model).add(noteIri, EVOC.NOTE_LITERAL, xmlLit, graph);
            verify(model).add(noteIri, RDF.VALUE, stringLit, graph);
            verifyNoMoreInteractions(model);
        }
    }
}