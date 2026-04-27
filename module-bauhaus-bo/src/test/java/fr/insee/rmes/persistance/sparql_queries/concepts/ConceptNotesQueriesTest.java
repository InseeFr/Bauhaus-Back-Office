package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.DatableNote;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class ConceptNotesQueriesTest {

    private ConceptNotesQueries conceptNotesQueries;

    @BeforeEach
    void setUp() {
        conceptNotesQueries = new ConceptNotesQueries(new ConfigStub());
    }

    @Test
    void shouldGetLastVersionnableNoteVersion() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            IRI predicat = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2004/02/skos/core#definition");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("getLastVersionnableNoteVersion.ftlh"), any(Map.class)))
                    .thenReturn("select ?version where { ... }");

            String result = conceptNotesQueries.getLastVersionnableNoteVersion("concept123", predicat);

            assertNotNull(result);
            assertEquals("select ?version where { ... }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("getLastVersionnableNoteVersion.ftlh"),
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "concept123".equals(map.get("CONCEPT_ID")) &&
                               predicat.equals(map.get("PREDICAT"));
                    })));
        }
    }

    @Test
    void shouldGetConceptVersion() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("getConceptVersion.ftlh"), any(Map.class)))
                    .thenReturn("select ?conceptVersion where { ... }");

            String result = conceptNotesQueries.getConceptVersion("concept456");

            assertNotNull(result);
            assertEquals("select ?conceptVersion where { ... }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("getConceptVersion.ftlh"),
                    argThat(params -> "concept456".equals(((Map<String, Object>) params).get("CONCEPT_ID")))));
        }
    }

    @Test
    void shouldGetChangeNoteToDelete() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("getChangeNoteToDelete.ftlh"), any(Map.class)))
                    .thenReturn("select ?changeNoteURI where { ... }");

            DatableNote note = new DatableNote();
            note.setLang("fr");
            note.setConceptVersion("3");

            String result = conceptNotesQueries.getChangeNoteToDelete("concept789", note);

            assertNotNull(result);
            assertEquals("select ?changeNoteURI where { ... }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("getChangeNoteToDelete.ftlh"),
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "concept789".equals(map.get("CONCEPT_ID")) &&
                               "fr".equals(map.get("LANG")) &&
                               "3".equals(map.get("CONCEPT_VERSION"));
                    })));
        }
    }

    @Test
    void shouldGetHistoricalNotes() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("getHistoricalNotes.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?note ?predicat WHERE { ... }");

            String result = conceptNotesQueries.getHistoricalNotes("conceptABC", "5");

            assertNotNull(result);
            assertEquals("SELECT ?note ?predicat WHERE { ... }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("getHistoricalNotes.ftlh"),
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "conceptABC".equals(map.get("CONCEPT_ID")) &&
                               "5".equals(map.get("MAX_VERSION")) &&
                               new ConfigStub().getConceptsGraph().equals(map.get("CONCEPTS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldCheckIsExist() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            IRI note = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/concepts/note/123");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("isNoteExist.ftlh"), any(Map.class)))
                    .thenReturn("ASK { <http://rdf.insee.fr/concepts/note/123> ?b ?c }");

            String result = conceptNotesQueries.isExist(note);

            assertNotNull(result);
            assertEquals("ASK { <http://rdf.insee.fr/concepts/note/123> ?b ?c }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("isNoteExist.ftlh"),
                    argThat(params -> note.equals(((Map<String, Object>) params).get("NOTE")))));
        }
    }

    @Test
    void shouldCheckIsClosed() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            IRI note = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/concepts/note/456");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("isNoteClosed.ftlh"), any(Map.class)))
                    .thenReturn("ASK { <http://rdf.insee.fr/concepts/note/456> insee:validUntil ?c }");

            String result = conceptNotesQueries.isClosed(note);

            assertNotNull(result);
            assertEquals("ASK { <http://rdf.insee.fr/concepts/note/456> insee:validUntil ?c }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("concepts/notes/"), eq("isNoteClosed.ftlh"),
                    argThat(params -> note.equals(((Map<String, Object>) params).get("NOTE")))));
        }
    }
}
