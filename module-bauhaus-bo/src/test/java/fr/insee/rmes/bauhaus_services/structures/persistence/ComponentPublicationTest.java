package fr.insee.rmes.bauhaus_services.structures.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * La publication recopie les triplets du composant de la base de gestion vers la base de
 * publication : ce qui compte est le tri entre les triplets recopiés tels quels, ceux dont l'URI
 * doit être réécrite, et ceux qui restent en gestion.
 */
@ExtendWith(MockitoExtension.class)
class ComponentPublicationTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final String GESTION = "http://gestion/";
    private static final String PUBLICATION = "http://publication/";

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    RepositoryPublication repositoryPublication;

    private ComponentPublication componentPublication;

    @BeforeEach
    void setUp() {
        componentPublication = new ComponentPublication(
                repoGestion, null, repositoryPublication, new PublicationUtils(GESTION, PUBLICATION, null, null));
    }

    @Test
    void shouldRewriteTheUriOfTheLinkedResourcesAndKeepTheLiteralsAsIs() throws RmesException {
        IRI component = VF.createIRI(GESTION + "composants/dimension/d1000");
        IRI codeList = VF.createIRI(GESTION + "codes/cl1000");
        givenStatements(
                VF.createStatement(component, RDFS.LABEL, VF.createLiteral("label fr", "fr")),
                VF.createStatement(component, QB.CODE_LIST, codeList));

        componentPublication.publishComponent(component, QB.DIMENSION_PROPERTY);

        Model model = publishedModel();
        IRI publishedComponent = VF.createIRI(PUBLICATION + "composants/dimension/d1000");
        assertThat(model.contains(publishedComponent, RDFS.LABEL, VF.createLiteral("label fr", "fr")))
                .isTrue();
        assertThat(model.contains(publishedComponent, QB.CODE_LIST, VF.createIRI(PUBLICATION + "codes/cl1000")))
                .isTrue();
    }

    /** L'état de validation, le créateur et les contributeurs n'appartiennent qu'à la base de gestion. */
    @Test
    void shouldNotPublishTheManagementOnlyAttributes() throws RmesException {
        IRI component = VF.createIRI(GESTION + "composants/dimension/d1000");
        givenStatements(
                VF.createStatement(
                        component,
                        VF.createIRI("http://rdf.insee.fr/def/base#validationState"),
                        VF.createLiteral("Validated")),
                VF.createStatement(component, DC.CREATOR, VF.createLiteral("DG75-F302")),
                VF.createStatement(component, DC.CONTRIBUTOR, VF.createLiteral("DG75-F302")),
                VF.createStatement(component, SKOS.NOTATION, VF.createLiteral("d1000")));

        componentPublication.publishComponent(component, QB.DIMENSION_PROPERTY);

        Model model = publishedModel();
        assertThat(model).hasSize(1);
        assertThat(model.contains(null, SKOS.NOTATION, VF.createLiteral("d1000")))
                .isTrue();
    }

    @Test
    void shouldReportARepositoryFailureAsAServerError() throws RmesException {
        IRI component = VF.createIRI(GESTION + "composants/dimension/d1000");
        when(repoGestion.getConnection()).thenReturn(null);
        when(repoGestion.getStatements(any(), eq(component))).thenThrow(new RepositoryException("boom"));

        RmesException exception = assertThrows(
                RmesException.class, () -> componentPublication.publishComponent(component, QB.DIMENSION_PROPERTY));

        assertThat(exception.getStatus()).isEqualTo(500);
    }

    private void givenStatements(Statement... statements) throws RmesException {
        when(repoGestion.getConnection()).thenReturn(null);
        when(repoGestion.getStatements(any(), any(IRI.class)))
                .thenReturn(new RepositoryResult<>(
                        new CloseableIteratorIteration<>(List.of(statements).iterator())));
    }

    private Model publishedModel() throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication).publishResource(any(), modelCaptor.capture(), any());
        return modelCaptor.getValue();
    }
}
