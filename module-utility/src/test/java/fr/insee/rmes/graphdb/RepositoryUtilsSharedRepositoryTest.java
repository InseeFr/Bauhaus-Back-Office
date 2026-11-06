package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.keycloak.TokenService;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Un seul {@link Repository} par base, prêté à chaque requête et fermé avec l'application. */
class RepositoryUtilsSharedRepositoryTest {

    private static final String SERVER = "http://localhost:7200";

    private final RepositoryUtils repositoryUtils = new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED);

    @Test
    void lends_the_same_repository_to_every_request_on_the_same_database() {
        assertThat(repositoryUtils.initRepository(SERVER, "gestion"))
                .isSameAs(repositoryUtils.initRepository(SERVER, "gestion"));
    }

    @Test
    void keeps_one_repository_per_database() {
        assertThat(repositoryUtils.initRepository(SERVER, "gestion"))
                .isNotSameAs(repositoryUtils.initRepository(SERVER, "publication"));
    }

    @Test
    void shuts_the_repositories_down_with_the_application() {
        var repository = new AtomicReference<Repository>();

        new ApplicationContextRunner()
                .withPropertyValues("fr.insee.rmes.bauhaus.rdf.auth=DISABLED")
                .withBean(TokenService.class, () -> mock(TokenService.class))
                .withBean(RepositoryUtils.class)
                .run(context ->
                        repository.set(context.getBean(RepositoryUtils.class).initRepository(SERVER, "gestion")));

        assertThat(repository.get().isInitialized()).isFalse();
    }

    @Test
    void renews_the_keycloak_token_on_the_shared_repository() {
        var tokenService = mock(TokenService.class);
        when(tokenService.getAccessToken()).thenReturn("premier", "second");
        when(tokenService.isTokenValid("premier")).thenReturn(false);
        var authenticated = new RepositoryUtils(tokenService, RepositoryInitiator.Type.ENABLED);

        var first = authenticated.initRepository(SERVER, "gestion");
        var second = authenticated.initRepository(SERVER, "gestion");

        assertThat(second).isSameAs(first);
        assertThat(((HTTPRepository) second).getAdditionalHttpHeaders())
                .containsEntry("Authorization", "bearer second");
    }
}
