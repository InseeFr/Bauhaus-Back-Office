package fr.insee.rmes.bauhaus_services.rdf_utils;

import static fr.insee.rmes.graphdb.RepositoryInitiator.Type.DISABLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.SubjectModelGraph;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Une écriture en échec dans la base de diffusion répondait le message RDF4J brut et le nom du
 * dépôt (ticket 14 de l'audit #1264) : l'utilisateur reçoit désormais un code traduit par le front,
 * le détail technique ne part que dans les logs.
 */
class RepositoryPublicationFailureTest {

    private static final String RDF4J_MESSAGE = "Connection refused: graphdb-diffusion:7200";
    private static final String REPOSITORY_NAME = "repository-publication-name";

    private final SimpleValueFactory factory = SimpleValueFactory.getInstance();
    private final IRI subject = factory.createIRI("http://example.org/subject");
    private final Model model = new LinkedHashModel();

    private RepositoryConnection connection;
    private RepositoryPublication repositoryPublication;

    @BeforeEach
    void setUp() {
        RepositoryUtils repositoryUtils = mock(RepositoryUtils.class);
        Repository repository = mock(Repository.class);
        connection = mock(RepositoryConnection.class);
        when(repository.toString()).thenReturn(REPOSITORY_NAME);
        when(repositoryUtils.authType()).thenReturn(DISABLED);
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(repository);
        when(repository.getConnection()).thenReturn(connection);
        repositoryPublication = new RepositoryPublication("http://graphdb:7200", "publication", repositoryUtils);
        model.add(subject, factory.createIRI("http://example.org/p"), factory.createLiteral("o"));
    }

    @Test
    void shouldReportAnUnavailableRepositoryWhenPublishingAConcept() {
        doThrow(new RepositoryException(RDF4J_MESSAGE)).when(connection).add(any(Model.class));

        assertUnavailableRepository(() -> repositoryPublication.publishConcept(subject, model, List.of(), List.of()));
    }

    @Test
    void shouldReportAnUnavailableRepositoryWhenClearingTheLinksOfAConcept() {
        doThrow(new RepositoryException(RDF4J_MESSAGE))
                .when(connection)
                .getStatements(any(), any(), any(Resource.class), anyBoolean());

        assertUnavailableRepository(() -> repositoryPublication.publishConcept(subject, model, List.of(), List.of()));
    }

    @Test
    void shouldReportAnUnavailableRepositoryWhenPublishingAResource() {
        doThrow(new RepositoryException(RDF4J_MESSAGE)).when(connection).add(any(Model.class));

        assertUnavailableRepository(() -> repositoryPublication.publishResource(subject, model, "operation"));
    }

    @Test
    void shouldReportAnUnavailableRepositoryWhenPublishingAGraph() {
        doThrow(new RepositoryException(RDF4J_MESSAGE)).when(connection).clear(any(Resource.class));

        assertUnavailableRepository(() -> repositoryPublication.publishContext(subject, model, "sims"));
    }

    @Test
    void shouldReportAnUnavailableRepositoryWhenOverridingTriplets() {
        doThrow(new RepositoryException(RDF4J_MESSAGE)).when(connection).add(any(Model.class));

        assertUnavailableRepository(() -> repositoryPublication.overrideTriplets(subject, model, subject));
    }

    @Test
    void shouldReportAnUnavailableRepositoryWhenOverridingTripletsInBulk() {
        doThrow(new RepositoryException(RDF4J_MESSAGE)).when(connection).begin();

        assertUnavailableRepository(() ->
                repositoryPublication.bulkOverrideTriplets(List.of(new SubjectModelGraph(subject, model, subject))));
    }

    private void assertUnavailableRepository(ThrowingCallable publication) {
        RmesException exception = catchThrowableOfType(RmesException.class, publication);

        assertThat(exception).as("publication should fail").isNotNull();
        assertThat(exception.getStatus()).isEqualTo(503);
        JSONObject body = new JSONObject(exception.getDetails());
        assertThat(body.getString("code")).isEqualTo("PUBLICATION_REPOSITORY_UNAVAILABLE");
        assertThat(body.getString("message"))
                .isEqualTo("Publication failed: the dissemination repository is unavailable. Please try again later.");
        assertThat(exception.getDetails()).doesNotContain(RDF4J_MESSAGE).doesNotContain(REPOSITORY_NAME);
        assertThat(exception).hasRootCauseInstanceOf(RepositoryException.class);
    }
}
