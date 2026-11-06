package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import fr.insee.rmes.keycloak.TokenService;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Le backend RDF est lu par Spring depuis {@code fr.insee.rmes.rdf.backend}, comme dans l'application. */
class RepositoryUtilsBackendPropertyTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("fr.insee.rmes.bauhaus.rdf.auth=DISABLED")
            .withBean(TokenService.class, () -> mock(TokenService.class))
            .withBean(RepositoryUtils.class);

    @Test
    void stays_on_graphdb_when_the_property_is_absent() {
        contextRunner.run(context -> assertThat(
                        context.getBean(RepositoryUtils.class).initRepository("http://localhost:7200", "gestion"))
                .isInstanceOf(HTTPRepository.class));
    }

    @Test
    void stays_on_graphdb_when_the_property_is_declared_as_in_the_local_configuration() {
        contextRunner
                .withPropertyValues("fr.insee.rmes.rdf.backend=graphdb")
                .run(context -> assertThat(context.getBean(RepositoryUtils.class)
                                .initRepository("http://localhost:7200", "gestion"))
                        .isInstanceOf(HTTPRepository.class));
    }

    @Test
    void talks_to_fuseki_through_the_sparql_protocol_when_the_property_says_so() {
        contextRunner
                .withPropertyValues("fr.insee.rmes.rdf.backend=fuseki")
                .run(context -> assertThat(context.getBean(RepositoryUtils.class)
                                .initRepository("http://localhost:3030", "bauhaus"))
                        .isInstanceOf(SPARQLRepository.class));
    }
}
