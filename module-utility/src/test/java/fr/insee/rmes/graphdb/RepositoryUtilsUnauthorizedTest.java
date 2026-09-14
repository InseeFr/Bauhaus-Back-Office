package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.keycloak.TokenService;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.Test;

/**
 * GraphDB répond 401 sans corps d'erreur : RDF4J lève alors une {@link UnauthorizedException}
 * sans message, ce qui produisait une 500 au corps vide. Ces tests vérifient que l'erreur
 * renvoyée nomme explicitement le mode d'authentification RDF configuré.
 */
class RepositoryUtilsUnauthorizedTest {

    private static final String QUERY = "SELECT ?s WHERE { ?s ?p ?o }";
    private static final String UPDATE_QUERY = "INSERT DATA { <http://example.org/s> <http://example.org/p> \"o\" . }";

    private final TokenService tokenService = mock(TokenService.class);

    @Test
    void shouldExplainThatRdfAuthIsDisabledWhenGraphDbAnswersUnauthorizedOnQuery() {
        RepositoryUtils repositoryUtils = new RepositoryUtils(tokenService, RepositoryInitiator.Type.DISABLED);

        assertThatThrownBy(() -> repositoryUtils.getResponse(QUERY, repositoryRefusingQuery()))
                .isInstanceOf(RmesException.class)
                .hasMessageContaining("401")
                .hasMessageContaining("fr.insee.rmes.bauhaus.rdf.auth=DISABLED");
    }

    @Test
    void shouldExplainThatTheTokenWasRejectedWhenRdfAuthIsEnabled() {
        RepositoryUtils repositoryUtils = new RepositoryUtils(tokenService, RepositoryInitiator.Type.ENABLED);

        assertThatThrownBy(() -> repositoryUtils.getResponse(QUERY, repositoryRefusingQuery()))
                .isInstanceOf(RmesException.class)
                .hasMessageContaining("401")
                .hasMessageContaining("fr.insee.rmes.bauhaus.rdf.auth=ENABLED");
    }

    @Test
    void shouldExplainThatRdfAuthIsDisabledWhenGraphDbAnswersUnauthorizedOnAskQuery() {
        RepositoryUtils repositoryUtils = new RepositoryUtils(tokenService, RepositoryInitiator.Type.DISABLED);

        assertThatThrownBy(() -> repositoryUtils.getResponseForAskQuery(QUERY, repositoryRefusingAskQuery()))
                .isInstanceOf(RmesException.class)
                .hasMessageContaining("fr.insee.rmes.bauhaus.rdf.auth=DISABLED");
    }

    @Test
    void shouldExplainThatRdfAuthIsDisabledWhenGraphDbAnswersUnauthorizedOnUpdate() {
        RepositoryUtils repositoryUtils = new RepositoryUtils(tokenService, RepositoryInitiator.Type.DISABLED);

        assertThatThrownBy(() -> repositoryUtils.executeUpdate(UPDATE_QUERY, repositoryRefusingUpdate()))
                .isInstanceOf(RmesException.class)
                .hasMessageContaining("fr.insee.rmes.bauhaus.rdf.auth=DISABLED");
    }

    @Test
    void shouldKeepTheRdf4jMessageWhenTheFailureIsNotAnAuthenticationOne() {
        RepositoryUtils repositoryUtils = new RepositoryUtils(tokenService, RepositoryInitiator.Type.DISABLED);
        Repository repository = mock(Repository.class);
        RepositoryConnection connection = mock(RepositoryConnection.class);
        when(repository.getConnection()).thenReturn(connection);
        when(connection.prepareTupleQuery(any(QueryLanguage.class), anyString()))
                .thenThrow(new MalformedQueryException("Lexical error"));

        assertThatThrownBy(() -> repositoryUtils.getResponse(QUERY, repository))
                .isInstanceOf(RmesException.class)
                .hasMessageContaining("Lexical error");
    }

    @Test
    void shouldExposeTheExplicitMessageAsExceptionDetails() {
        RepositoryUtils repositoryUtils = new RepositoryUtils(tokenService, RepositoryInitiator.Type.DISABLED);

        assertThatThrownBy(() -> repositoryUtils.getResponse(QUERY, repositoryRefusingQuery()))
                .isInstanceOfSatisfying(
                        RmesException.class,
                        exception ->
                                assertThat(exception.getDetails()).contains("fr.insee.rmes.bauhaus.rdf.auth=DISABLED"));
    }

    private static Repository repositoryRefusingQuery() {
        Repository repository = mock(Repository.class);
        RepositoryConnection connection = mock(RepositoryConnection.class);
        when(repository.getConnection()).thenReturn(connection);
        when(connection.prepareTupleQuery(any(QueryLanguage.class), anyString()))
                .thenThrow(new UnauthorizedException());
        return repository;
    }

    private static Repository repositoryRefusingAskQuery() {
        Repository repository = mock(Repository.class);
        RepositoryConnection connection = mock(RepositoryConnection.class);
        when(repository.getConnection()).thenReturn(connection);
        when(connection.prepareBooleanQuery(any(QueryLanguage.class), anyString()))
                .thenThrow(new UnauthorizedException());
        return repository;
    }

    private static Repository repositoryRefusingUpdate() {
        Repository repository = mock(Repository.class);
        RepositoryConnection connection = mock(RepositoryConnection.class);
        when(repository.getConnection()).thenReturn(connection);
        when(connection.prepareUpdate(any(QueryLanguage.class), anyString())).thenThrow(new UnauthorizedException());
        return repository;
    }
}
