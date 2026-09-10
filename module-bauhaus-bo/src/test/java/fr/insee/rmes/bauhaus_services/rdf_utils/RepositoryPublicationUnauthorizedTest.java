package fr.insee.rmes.bauhaus_services.rdf_utils;

import static fr.insee.rmes.graphdb.RepositoryInitiator.Type.DISABLED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryUtils;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Les écritures en base de publication passent par leurs propres blocs {@code catch} :
 * un 401 de GraphDB y produisait, comme sur les requêtes, une 500 sans message.
 */
class RepositoryPublicationUnauthorizedTest {

    private static final String AUTH_DISABLED = "fr.insee.rmes.bauhaus.rdf.auth=DISABLED";

    private final IRI graph = SimpleValueFactory.getInstance().createIRI("http://example.org/graph");
    private final Model model = new LinkedHashModel();

    private RepositoryConnection connection;
    private RepositoryPublication repositoryPublication;

    @BeforeEach
    void setUp() {
        RepositoryUtils repositoryUtils = mock(RepositoryUtils.class);
        Repository repository = mock(Repository.class);
        connection = mock(RepositoryConnection.class);
        when(repositoryUtils.authType()).thenReturn(DISABLED);
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(repository);
        when(repository.getConnection()).thenReturn(connection);
        repositoryPublication = new RepositoryPublication("http://graphdb:7200", "publication", repositoryUtils);
    }

    @Test
    void shouldExplainThatRdfAuthIsDisabledWhenPublishingAContext() {
        doThrow(new UnauthorizedException()).when(connection).clear(any(Resource.class));

        assertThatThrownBy(() -> repositoryPublication.publishContext(graph, model, "concept"))
                .isInstanceOf(RmesException.class)
                .hasMessageContaining(AUTH_DISABLED);
    }

    @Test
    void shouldExplainThatRdfAuthIsDisabledWhenPublishingAResource() {
        doThrow(new UnauthorizedException()).when(connection).remove(any(Resource.class), any(), any());

        assertThatThrownBy(() -> repositoryPublication.publishResource(graph, model, "concept"))
                .isInstanceOf(RmesException.class)
                .hasMessageContaining(AUTH_DISABLED);
    }

    @Test
    void shouldExplainThatRdfAuthIsDisabledWhenOverridingTriplets() {
        model.add(
                graph,
                SimpleValueFactory.getInstance().createIRI("http://example.org/p"),
                SimpleValueFactory.getInstance().createLiteral("o"));
        doThrow(new UnauthorizedException())
                .when(connection)
                .remove(any(Resource.class), any(), any(), any(Resource.class));

        assertThatThrownBy(() -> repositoryPublication.overrideTriplets(graph, model, graph))
                .isInstanceOf(RmesException.class)
                .hasMessageContaining(AUTH_DISABLED);
    }
}
