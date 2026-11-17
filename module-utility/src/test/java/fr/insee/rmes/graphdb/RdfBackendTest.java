package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RdfBackendTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void stays_on_graphdb_when_the_property_has_no_value(String value) {
        assertThat(RdfBackend.fromProperty(value)).isEqualTo(RdfBackend.GRAPHDB);
    }

    @ParameterizedTest
    @ValueSource(strings = {"graphdb", "GRAPHDB", " GraphDB "})
    void reads_graphdb_whatever_its_case(String value) {
        assertThat(RdfBackend.fromProperty(value)).isEqualTo(RdfBackend.GRAPHDB);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fuseki", "FUSEKI"})
    void reads_fuseki_whatever_its_case(String value) {
        assertThat(RdfBackend.fromProperty(value)).isEqualTo(RdfBackend.FUSEKI);
    }

    @Test
    void rejects_an_unknown_backend_by_naming_the_property_and_the_accepted_values() {
        assertThatThrownBy(() -> RdfBackend.fromProperty("virtuoso"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fr.insee.rmes.rdf.backend=virtuoso")
                .hasMessageContaining("graphdb")
                .hasMessageContaining("fuseki");
    }
}
