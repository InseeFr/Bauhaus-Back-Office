package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.rdf4j.repository.http.HTTPRepository;
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
    void refuses_fuseki_until_an_initiator_can_talk_to_it() {
        assertThatThrownBy(() ->
                        RepositoryInitiator.newInstance(RdfBackend.FUSEKI, RepositoryInitiator.Type.DISABLED, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fr.insee.rmes.rdf.backend=fuseki");
    }
}
