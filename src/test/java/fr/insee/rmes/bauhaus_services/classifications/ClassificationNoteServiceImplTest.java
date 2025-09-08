package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.EVOC;
import fr.insee.rmes.persistance.ontologies.XKOS;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassificationNoteServiceImplTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @InjectMocks
    private ClassificationNoteServiceImpl service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddNotes_shouldDeleteAndAddTriplets_whenValueIsPresent() throws RmesException {
        // Given
        String iri = "http://example.org/note";
        String value = "This is **markdown** content.";
        Model model = new LinkedHashModel();
        Resource graph = new InternedIRI("namespaceGraph","localNameGraph");
        // When
        service.addNotes(graph, iri, value, model);

        // Then
        IRI noteIri = RdfUtils.createIRI(iri);

        verify(repositoryGestion).deleteTripletByPredicate(noteIri, EVOC.NOTE_LITERAL, graph, null);
        verify(repositoryGestion).deleteTripletByPredicate(noteIri, XKOS.PLAIN_TEXT, graph, null);
        verify(repositoryGestion).deleteTripletByPredicate(noteIri, RDF.VALUE, graph, null);

        assertEquals("[(http://example.org/note, http://eurovoc.europa.eu/schema#noteLiteral, \"<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>This is <strong>markdown</strong> content.</p></div>\") [namespaceGraphlocalNameGraph], (http://example.org/note, http://rdf-vocabulary.ddialliance.org/xkos#plainText, \"This is markdown content.\") [namespaceGraphlocalNameGraph], (http://example.org/note, http://www.w3.org/1999/02/22-rdf-syntax-ns#value, \"This is **markdown** content.\") [namespaceGraphlocalNameGraph]]", model.toString());

    }

    @Test
    void testAddNotes_shouldOnlyDelete_whenValueIsEmpty() throws RmesException {
        String iri = "http://example.org/note";
        String value = ""; // empty
        Model model = new LinkedHashModel();
        Resource graph = new InternedIRI("namespaceGraph","localNameGraph");

        service.addNotes(graph, iri, value, model);

        IRI noteIri = RdfUtils.createIRI(iri);

        verify(repositoryGestion).deleteTripletByPredicate(noteIri, EVOC.NOTE_LITERAL, graph, null);
        verify(repositoryGestion).deleteTripletByPredicate(noteIri, XKOS.PLAIN_TEXT, graph, null);
        verify(repositoryGestion).deleteTripletByPredicate(noteIri, RDF.VALUE, graph, null);

        assertEquals("[]", model.toString());

    }

    @Test
    void testAddNotes_shouldDoNothing_whenIriIsEmpty() throws RmesException {
        String value = "some value";
        Model model = new LinkedHashModel();
        Resource graph = new InternedIRI("namespaceGraph","localNameGraph");

        service.addNotes(graph, null, value, model);

        verifyNoInteractions(repositoryGestion);
        assertEquals("[]", model.toString());
    }
}