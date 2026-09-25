package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class GraphDBCollectionRepositoryTest {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private static final IRI GRAPH = VF.createIRI("http://example.com/graph/concepts");
    private static final Literal LIT = VF.createLiteral("dummy");

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
        repo = new GraphDBCollectionRepository(repositoryGestion, new BauhausLanguagesProperties(LG1, LG2));
    }

    @Test
    void save_should_build_model_and_call_repository_with_mandatory_and_optional_triples() throws Exception {
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

        IRI subj = VF.createIRI("http://example.com/collection/c1");
        IRI m1 = VF.createIRI("http://example.com/concept/m1");
        IRI m2 = VF.createIRI("http://example.com/concept/m2");

        try (MockedStatic<RdfUtils> utils = mockStatic(RdfUtils.class)) {
            stubRdfUtils(utils, "c1", subj);
            utils.when(() -> RdfUtils.conceptIRI("m1")).thenReturn(m1);
            utils.when(() -> RdfUtils.conceptIRI("m2")).thenReturn(m2);

            String returnedId = repo.save(col);

            Assertions.assertEquals("c1", returnedId);
            verify(repositoryGestion).loadSimpleObject(iriCaptor.capture(), modelCaptor.capture());
            assertThat(iriCaptor.getValue()).isEqualTo(subj);

            Model model = modelCaptor.getValue();

            assertThat(model.contains(subj, RDF.TYPE, SKOS.COLLECTION, GRAPH)).isTrue();
            assertThat(model.contains(subj, INSEE.VALIDATION_STATE, LIT, GRAPH)).isTrue();
            assertThat(model.contains(subj, DCTERMS.TITLE, LIT, GRAPH)).isTrue(); // lg1
            assertThat(model.contains(subj, DCTERMS.CREATED, LIT, GRAPH)).isTrue();
            assertThat(model.contains(subj, DC.CONTRIBUTOR, LIT, GRAPH)).isTrue();
            assertThat(model.contains(subj, DC.CREATOR, LIT, GRAPH)).isTrue();

            assertThat(model.contains(subj, SKOS.MEMBER, m1, GRAPH)).isTrue();
            assertThat(model.contains(subj, SKOS.MEMBER, m2, GRAPH)).isTrue();

            utils.verify(() -> RdfUtils.addTripleDateTime(
                    eq(subj), eq(DCTERMS.MODIFIED), isNull(), any(LinkedHashModel.class), eq(GRAPH)));
            utils.verify(() -> RdfUtils.addTripleString(
                    eq(subj), eq(DCTERMS.TITLE), eq("Title EN"), eq(LG2), any(LinkedHashModel.class), eq(GRAPH)));
            utils.verify(() -> RdfUtils.addTripleString(
                    eq(subj), eq(DCTERMS.DESCRIPTION), eq("Desc FR"), eq(LG1), any(LinkedHashModel.class), eq(GRAPH)));
            utils.verify(() -> RdfUtils.addTripleString(
                    eq(subj), eq(DCTERMS.DESCRIPTION), eq("Desc EN"), eq(LG2), any(LinkedHashModel.class), eq(GRAPH)));
        }
    }

    @Test
    void save_should_add_modified_when_provided() throws Exception {
        Collection col = mock(Collection.class);
        when(col.getId()).thenReturn("c2");
        when(col.getIsValidated()).thenReturn(false);
        when(col.getPrefLabelLg1()).thenReturn("FR");
        when(col.getCreated()).thenReturn("2025-09-01T10:00:00");
        when(col.getModified()).thenReturn("2025-09-02T11:00:00");
        when(col.getMembers()).thenReturn(List.of());

        IRI subj = VF.createIRI("http://example.com/collection/c2");

        try (MockedStatic<RdfUtils> utils = mockStatic(RdfUtils.class)) {
            stubRdfUtils(utils, "c2", subj);

            repo.save(col);

            verify(repositoryGestion).loadSimpleObject(any(IRI.class), any(Model.class));
            utils.verify(() -> RdfUtils.addTripleDateTime(
                    eq(subj), eq(DCTERMS.MODIFIED), eq("2025-09-02T11:00:00"), any(LinkedHashModel.class), eq(GRAPH)));
        }
    }

    @Test
    void save_should_return_id_and_invoke_repository_once() throws RmesException {
        Collection col = mock(Collection.class);
        when(col.getId()).thenReturn("c3");
        when(col.getIsValidated()).thenReturn(false);
        when(col.getPrefLabelLg1()).thenReturn("FR");
        when(col.getCreated()).thenReturn("2025-09-01T10:00:00");
        when(col.getMembers()).thenReturn(List.of());

        IRI subj = VF.createIRI("http://example.com/collection/c3");

        try (MockedStatic<RdfUtils> utils = mockStatic(RdfUtils.class)) {
            stubRdfUtils(utils, "c3", subj);

            GraphDBCollectionRepository repo =
                    new GraphDBCollectionRepository(repositoryGestion, new BauhausLanguagesProperties("fr", "en"));

            String id = repo.save(col);

            Assertions.assertEquals("c3", id);
            verify(repositoryGestion, times(1)).loadSimpleObject(any(IRI.class), any(Model.class));
            utils.verify(() -> RdfUtils.collectionIRI("c3"), times(1));
        }
    }

    /**
     * La collection {@code id} a pour IRI {@code subj} dans {@link #GRAPH} ; toutes les fabriques de
     * littéraux renvoient {@link #LIT} et les ajouts de triplets facultatifs ne font rien.
     */
    private static void stubRdfUtils(MockedStatic<RdfUtils> utils, String id, IRI subj) {
        utils.when(() -> RdfUtils.collectionIRI(id)).thenReturn(subj);
        utils.when(RdfUtils::conceptGraph).thenReturn(GRAPH);

        utils.when(() -> RdfUtils.setLiteralBoolean(any())).thenReturn(LIT);
        utils.when(() -> RdfUtils.setLiteralDateTime(nullable(String.class))).thenReturn(LIT);
        utils.when(() -> RdfUtils.setLiteralString(nullable(String.class))).thenReturn(LIT); // (String) accepte null
        utils.when(() -> RdfUtils.setLiteralString(any(ValidationStatus.class))).thenReturn(LIT); // (ValidationStatus)
        utils.when(() -> RdfUtils.setLiteralString(any(), any())).thenReturn(LIT); // (String, lang)

        utils.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(LinkedHashModel.class), any()))
                .thenAnswer(inv -> null);
        utils.when(() -> RdfUtils.addTripleString(any(), any(), any(), anyString(), any(LinkedHashModel.class), any()))
                .thenAnswer(inv -> null);
    }
}
