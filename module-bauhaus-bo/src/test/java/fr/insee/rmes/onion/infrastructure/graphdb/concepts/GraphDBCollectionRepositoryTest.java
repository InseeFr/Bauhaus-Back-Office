package fr.insee.rmes.onion.infrastructure.graphdb.concepts;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.INSEE;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphDBCollectionRepositoryTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @Captor
    ArgumentCaptor<Model> modelCaptor;

    @Captor
    ArgumentCaptor<IRI> iriCaptor;

    private GraphDBCollectionRepository repo;

    private final String LG1 = "fr";
    private final String LG2 = "en";

    @BeforeEach
    void setUp() {
        repo = new GraphDBCollectionRepository(repositoryGestion, LG1, LG2);
    }

    @Test
    void save_shouldBuildModelAndCallRepository_withMandatoryAndOptionalTriples() throws Exception {
        Collection col = mock(Collection.class);
        when(col.getId()).thenReturn("c1");
        when(col.getIsValidated()).thenReturn(true);
        when(col.getPrefLabelLg1()).thenReturn("Titre FR");
        when(col.getPrefLabelLg2()).thenReturn("Title EN");
        when(col.getCreated()).thenReturn("2025-09-01T10:00:00");
        when(col.getContributor()).thenReturn("Alice");
        when(col.getCreator()).thenReturn("Bob");
        when(col.getDescriptionLg1()).thenReturn("Desc FR");
        when(col.getDescriptionLg2()).thenReturn("Desc EN");
        when(col.getModified()).thenReturn(null); // pas de modified
        when(col.getMembers()).thenReturn(List.of("m1", "m2"));

        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI subj  = vf.createIRI("http://example.com/collection/c1");
        IRI graph = vf.createIRI("http://example.com/graph/concepts");
        IRI m1    = vf.createIRI("http://example.com/concept/m1");
        IRI m2    = vf.createIRI("http://example.com/concept/m2");
        Literal lit = vf.createLiteral("dummy");

        try (MockedStatic<RdfUtils> utils = mockStatic(RdfUtils.class)) {
            utils.when(() -> RdfUtils.collectionIRI("c1")).thenReturn(subj);
            utils.when(RdfUtils::conceptGraph).thenReturn(graph);
            utils.when(() -> RdfUtils.conceptIRI("m1")).thenReturn(m1);
            utils.when(() -> RdfUtils.conceptIRI("m2")).thenReturn(m2);

            utils.when(() -> RdfUtils.setLiteralBoolean(any())).thenReturn(lit);
            utils.when(() -> RdfUtils.setLiteralDateTime(nullable(String.class))).thenReturn(lit);
            utils.when(() -> RdfUtils.setLiteralString(nullable(String.class))).thenReturn(lit); // (String) accepte null
            utils.when(() -> RdfUtils.setLiteralString(any(), any())).thenReturn(lit);          // (String, lang)

            utils.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(LinkedHashModel.class), any()))
                    .thenAnswer(inv -> null);
            utils.when(() -> RdfUtils.addTripleString(any(), any(), any(), anyString(), any(LinkedHashModel.class), any()))
                    .thenAnswer(inv -> null);

            String returnedId = repo.save(col);

            Assertions.assertEquals("c1", returnedId);
            verify(repositoryGestion).loadSimpleObject(iriCaptor.capture(), modelCaptor.capture());
            assertThat(iriCaptor.getValue()).isEqualTo(subj);

            Model model = modelCaptor.getValue();

            assertThat(model.contains(subj, RDF.TYPE, SKOS.COLLECTION, graph)).isTrue();
            assertThat(model.contains(subj, INSEE.IS_VALIDATED, lit, graph)).isTrue();
            assertThat(model.contains(subj, DCTERMS.TITLE, lit, graph)).isTrue();   // lg1
            assertThat(model.contains(subj, DCTERMS.CREATED, lit, graph)).isTrue();
            assertThat(model.contains(subj, DC.CONTRIBUTOR, lit, graph)).isTrue();
            assertThat(model.contains(subj, DC.CREATOR, lit, graph)).isTrue();

            assertThat(model.contains(subj, SKOS.MEMBER, m1, graph)).isTrue();
            assertThat(model.contains(subj, SKOS.MEMBER, m2, graph)).isTrue();

            utils.verify(() -> RdfUtils.addTripleDateTime(eq(subj), eq(DCTERMS.MODIFIED), isNull(),
                    any(LinkedHashModel.class), eq(graph)));
            utils.verify(() -> RdfUtils.addTripleString(eq(subj), eq(DCTERMS.TITLE), eq("Title EN"), eq(LG2),
                    any(LinkedHashModel.class), eq(graph)));
            utils.verify(() -> RdfUtils.addTripleString(eq(subj), eq(DCTERMS.DESCRIPTION), eq("Desc FR"), eq(LG1),
                    any(LinkedHashModel.class), eq(graph)));
            utils.verify(() -> RdfUtils.addTripleString(eq(subj), eq(DCTERMS.DESCRIPTION), eq("Desc EN"), eq(LG2),
                    any(LinkedHashModel.class), eq(graph)));
        }
    }

    @Test
    void save_shouldAddModified_whenProvided() throws Exception {
        Collection col = mock(Collection.class);
        when(col.getId()).thenReturn("c2");
        when(col.getIsValidated()).thenReturn(false);
        when(col.getPrefLabelLg1()).thenReturn("FR");
        when(col.getCreated()).thenReturn("2025-09-01T10:00:00");
        when(col.getModified()).thenReturn("2025-09-02T11:00:00");
        when(col.getMembers()).thenReturn(List.of());

        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI subj  = vf.createIRI("http://example.com/collection/c2");
        IRI graph = vf.createIRI("http://example.com/graph/concepts");
        Literal lit = vf.createLiteral("dummy");

        try (MockedStatic<RdfUtils> utils = mockStatic(RdfUtils.class)) {
            utils.when(() -> RdfUtils.collectionIRI("c2")).thenReturn(subj);
            utils.when(RdfUtils::conceptGraph).thenReturn(graph);

            utils.when(() -> RdfUtils.setLiteralBoolean(any())).thenReturn(lit);
            utils.when(() -> RdfUtils.setLiteralDateTime(nullable(String.class))).thenReturn(lit);
            utils.when(() -> RdfUtils.setLiteralString(nullable(String.class))).thenReturn(lit);
            utils.when(() -> RdfUtils.setLiteralString(any(), any())).thenReturn(lit);

            utils.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(LinkedHashModel.class), any()))
                    .thenAnswer(inv -> null);
            utils.when(() -> RdfUtils.addTripleString(any(), any(), any(), anyString(), any(LinkedHashModel.class), any()))
                    .thenAnswer(inv -> null);

            repo.save(col);

            verify(repositoryGestion).loadSimpleObject(any(IRI.class), any(Model.class));
            utils.verify(() -> RdfUtils.addTripleDateTime(eq(subj), eq(DCTERMS.MODIFIED), eq("2025-09-02T11:00:00"),
                    any(LinkedHashModel.class), eq(graph)));
        }
    }

    @Test
    void save_shouldReturnIdAndInvokeRepository_once() throws RmesException {
        Collection col = mock(Collection.class);
        when(col.getId()).thenReturn("c3");
        when(col.getIsValidated()).thenReturn(false);
        when(col.getPrefLabelLg1()).thenReturn("FR");
        when(col.getCreated()).thenReturn("2025-09-01T10:00:00");
        when(col.getMembers()).thenReturn(List.of());

        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI subj = vf.createIRI("http://example.com/collection/c3");
        IRI graph = vf.createIRI("http://example.com/graph/concepts");
        Literal lit = vf.createLiteral("dummy");

        try (MockedStatic<RdfUtils> utils = mockStatic(RdfUtils.class)) {
            utils.when(() -> RdfUtils.collectionIRI("c3")).thenReturn(subj);
            utils.when(RdfUtils::conceptGraph).thenReturn(graph);

            utils.when(() -> RdfUtils.setLiteralBoolean(any())).thenReturn(lit);
            utils.when(() -> RdfUtils.setLiteralString(any(), any())).thenReturn(lit);
            utils.when(() -> RdfUtils.setLiteralString(nullable(String.class))).thenReturn(lit);
            utils.when(() -> RdfUtils.setLiteralDateTime(any())).thenReturn(lit);

            utils.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(LinkedHashModel.class), any()))
                    .thenAnswer(inv -> null);
            utils.when(() -> RdfUtils.addTripleString(any(), any(), any(), anyString(), any(LinkedHashModel.class), any()))
                    .thenAnswer(inv -> null);

            GraphDBCollectionRepository repo = new GraphDBCollectionRepository(repositoryGestion, "fr", "en");

            String id = repo.save(col);

            Assertions.assertEquals("c3", id);
            verify(repositoryGestion, times(1)).loadSimpleObject(any(IRI.class), any(Model.class));
            utils.verify(() -> RdfUtils.collectionIRI("c3"), times(1));
        }
    }
 }
