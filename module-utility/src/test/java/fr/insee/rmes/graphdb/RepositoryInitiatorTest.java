package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.jupiter.api.Test;

class RepositoryInitiatorTest {

    @Test
    void talks_to_graphdb_through_the_rdf4j_rest_protocol() throws Exception {
        var initiator = RepositoryInitiator.newInstance(RdfBackend.GRAPHDB, RepositoryInitiator.Type.DISABLED, null);

        assertThat(initiator.initRepository("http://localhost:7200", "gestion")).isInstanceOf(HTTPRepository.class);
    }

    @Test
    void keeps_the_token_authentication_of_graphdb_orthogonal_to_the_backend() {
        var initiator = RepositoryInitiator.newInstance(RdfBackend.GRAPHDB, RepositoryInitiator.Type.ENABLED, null);

        assertThat(initiator).isInstanceOf(RepositoryInitiatorWithAuthent.class);
    }

    @Test
    void talks_to_fuseki_through_the_sparql_protocol() throws Exception {
        var initiator = RepositoryInitiator.newInstance(RdfBackend.FUSEKI, RepositoryInitiator.Type.DISABLED, null);

        assertThat(initiator.initRepository("http://localhost:3030", "bauhaus")).isInstanceOf(SPARQLRepository.class);
    }

    @Test
    void refuses_the_keycloak_token_on_fuseki() {
        assertThatThrownBy(() ->
                        RepositoryInitiator.newInstance(RdfBackend.FUSEKI, RepositoryInitiator.Type.ENABLED, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fr.insee.rmes.rdf.backend=fuseki")
                .hasMessageContaining("fr.insee.rmes.bauhaus.rdf.auth=ENABLED");
    }
}
