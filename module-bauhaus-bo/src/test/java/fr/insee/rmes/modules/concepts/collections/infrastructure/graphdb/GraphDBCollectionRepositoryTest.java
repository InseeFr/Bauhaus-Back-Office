package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.config.BauhausIriFactoryStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphDBCollectionRepositoryTest {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private static final BauhausIriFactory IRI_FACTORY = BauhausIriFactoryStub.stub("concepts/definitions");
    private static final Resource GRAPH = IRI_FACTORY.conceptGraph();

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
        repo = new GraphDBCollectionRepository(
                repositoryGestion, new BauhausLanguagesProperties(LG1, LG2), IRI_FACTORY);
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

        IRI subj = VF.createIRI("http://bauhaus/concepts/definitions/c1");
        IRI m1 = VF.createIRI("http://bauhaus/concepts/definitions/m1");
        IRI m2 = VF.createIRI("http://bauhaus/concepts/definitions/m2");

        String returnedId = repo.save(col);

        Assertions.assertEquals("c1", returnedId);
        verify(repositoryGestion).loadSimpleObject(iriCaptor.capture(), modelCaptor.capture());
        assertThat(iriCaptor.getValue()).isEqualTo(subj);

        Model model = modelCaptor.getValue();

        assertThat(model.contains(subj, RDF.TYPE, SKOS.COLLECTION, GRAPH)).isTrue();
        assertThat(model.contains(subj, INSEE.VALIDATION_STATE, RdfTriples.string(ValidationStatus.VALIDATED), GRAPH))
                .isTrue();
        assertThat(model.contains(subj, DCTERMS.TITLE, RdfTriples.string("Titre FR", LG1), GRAPH))
                .isTrue();
        assertThat(model.contains(subj, DCTERMS.CREATED, RdfTriples.dateTime("2025-09-01T10:00:00"), GRAPH))
                .isTrue();
        assertThat(model.contains(subj, DC.CONTRIBUTOR, RdfTriples.string("Alice"), GRAPH))
                .isTrue();
        assertThat(model.contains(subj, DC.CREATOR, RdfTriples.string("Bob"), GRAPH))
                .isTrue();

        assertThat(model.contains(subj, SKOS.MEMBER, m1, GRAPH)).isTrue();
        assertThat(model.contains(subj, SKOS.MEMBER, m2, GRAPH)).isTrue();

        assertThat(model.contains(subj, DCTERMS.TITLE, RdfTriples.string("Title EN", LG2), GRAPH))
                .isTrue();
        assertThat(model.contains(subj, DCTERMS.DESCRIPTION, RdfTriples.string("Desc FR", LG1), GRAPH))
                .isTrue();
        assertThat(model.contains(subj, DCTERMS.DESCRIPTION, RdfTriples.string("Desc EN", LG2), GRAPH))
                .isTrue();
        // seule la date du jour, toujours écrite : pas de dcterms:modified en xsd:dateTime
        assertThat(model.filter(subj, DCTERMS.MODIFIED, null, GRAPH)).hasSize(1);
    }

    @Test
    void save_should_add_modified_when_provided() throws Exception {
        Collection col = mock(Collection.class);
        when(col.getId()).thenReturn("c2");
        when(col.getIsValidated()).thenReturn(false);
        when(col.getPrefLabelLg1()).thenReturn("FR");
        when(col.getCreated()).thenReturn("2025-09-01T10:00:00");
        when(col.getContributor()).thenReturn("Alice");
        when(col.getCreator()).thenReturn("Bob");
        when(col.getModified()).thenReturn("2025-09-02T11:00:00");
        when(col.getMembers()).thenReturn(List.of());

        IRI subj = VF.createIRI("http://bauhaus/concepts/definitions/c2");

        repo.save(col);

        verify(repositoryGestion).loadSimpleObject(iriCaptor.capture(), modelCaptor.capture());
        assertThat(iriCaptor.getValue()).isEqualTo(subj);
        assertThat(modelCaptor
                        .getValue()
                        .contains(subj, DCTERMS.MODIFIED, RdfTriples.dateTime("2025-09-02T11:00:00"), GRAPH))
                .isTrue();
    }

    @Test
    void save_should_return_id_and_invoke_repository_once() throws RmesException {
        Collection col = mock(Collection.class);
        when(col.getId()).thenReturn("c3");
        when(col.getIsValidated()).thenReturn(false);
        when(col.getPrefLabelLg1()).thenReturn("FR");
        when(col.getCreated()).thenReturn("2025-09-01T10:00:00");
        when(col.getContributor()).thenReturn("Alice");
        when(col.getCreator()).thenReturn("Bob");
        when(col.getMembers()).thenReturn(List.of());

        IRI subj = VF.createIRI("http://bauhaus/concepts/definitions/c3");

        String id = repo.save(col);

        Assertions.assertEquals("c3", id);
        verify(repositoryGestion, times(1)).loadSimpleObject(eq(subj), any(Model.class));
    }
}
