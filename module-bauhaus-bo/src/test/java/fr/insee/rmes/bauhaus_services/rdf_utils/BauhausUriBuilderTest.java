package fr.insee.rmes.bauhaus_services.rdf_utils;

import static fr.insee.rmes.PropertiesKeys.OPERATIONS_BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class BauhausUriBuilderTest {

    private final BauhausUriBuilder.PropertiesFinder propertiesFinder =
            name -> OPERATIONS_BASE_URI.equals(name) ? Optional.of("operations/operation") : Optional.empty();

    private final BauhausUriBuilder bauhausUriBuilder =
            new BauhausUriBuilder("http://id.insee.fr/", "http://bauhaus/", propertiesFinder);

    @Test
    void getCompleteUriGestion_buildsGestionScopedOperationIri() {
        assertThat(bauhausUriBuilder.getCompleteUriGestion("operation", "s1250"))
                .isEqualTo("http://bauhaus/operations/operation/s1250");
    }

    @Test
    void getCompleteUriPublication_buildsPublicationScopedOperationIri() {
        assertThat(bauhausUriBuilder.getCompleteUriPublication("operation", "s1250"))
                .isEqualTo("http://id.insee.fr/operations/operation/s1250");
    }
}
