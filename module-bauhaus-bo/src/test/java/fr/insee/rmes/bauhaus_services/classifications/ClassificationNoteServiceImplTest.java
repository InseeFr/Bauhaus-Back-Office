package fr.insee.rmes.bauhaus_services.classifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.EVOC;
import fr.insee.rmes.graphdb.ontologies.XKOS;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassificationNoteServiceImplTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @InjectMocks
    private ClassificationNoteServiceImpl service;

    @Test
    void testAddNotes_shouldDeleteAndAddTriplets_whenValueIsPresent() throws RmesException {
        // Given
        String iri = "http://example.org/note";
        String value = "This is **markdown** content.";
        Model model = new LinkedHashModel();
        Resource graph = graph();
        // When
        service.addNotes(graph, iri, value, model);

        // Then
        verifyPreviousNoteTripletsDeleted(RdfUtils.createIRI(iri), graph);

        assertEquals(
                "[(http://example.org/note, http://eurovoc.europa.eu/schema#noteLiteral, \"<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>This is <strong>markdown</strong> content.</p></div>\") [namespaceGraphlocalNameGraph], (http://example.org/note, http://rdf-vocabulary.ddialliance.org/xkos#plainText, \"This is markdown content.\") [namespaceGraphlocalNameGraph], (http://example.org/note, http://www.w3.org/1999/02/22-rdf-syntax-ns#value, \"This is **markdown** content.\") [namespaceGraphlocalNameGraph]]",
                model.toString());
    }

    @Test
    void testAddNotes_shouldOnlyDelete_whenValueIsEmpty() throws RmesException {
        String iri = "http://example.org/note";
        String value = ""; // empty
        Model model = new LinkedHashModel();
        Resource graph = graph();

        service.addNotes(graph, iri, value, model);

        verifyPreviousNoteTripletsDeleted(RdfUtils.createIRI(iri), graph);

        assertEquals("[]", model.toString());
    }

    @Test
    void testAddNotes_shouldDoNothing_whenIriIsEmpty() throws RmesException {
        String value = "some value";
        Model model = new LinkedHashModel();
        Resource graph = graph();

        service.addNotes(graph, null, value, model);

        verifyNoInteractions(repositoryGestion);
        assertEquals("[]", model.toString());
    }

    private static Resource graph() {
        return new InternedIRI("namespaceGraph", "localNameGraph");
    }

    private void verifyPreviousNoteTripletsDeleted(IRI noteIri, Resource graph) throws RmesException {
        verify(repositoryGestion).deleteTripletByPredicate(noteIri, EVOC.NOTE_LITERAL, graph, null);
        verify(repositoryGestion).deleteTripletByPredicate(noteIri, XKOS.PLAIN_TEXT, graph, null);
        verify(repositoryGestion).deleteTripletByPredicate(noteIri, RDF.VALUE, graph, null);
    }
}
