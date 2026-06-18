package fr.insee.rmes.bauhaus_services.rdf_utils;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static fr.insee.rmes.PropertiesKeys.OPERATIONS_BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;

class UriUtilsTest {

    private final UriUtils.PropertiesFinder propertiesFinder = name ->
            OPERATIONS_BASE_URI.equals(name) ? Optional.of("operations/operation") : Optional.empty();

    private final UriUtils uriUtils = new UriUtils(
            "http://id.insee.fr/",
            "http://bauhaus/",
            propertiesFinder
    );

    @Test
    void getCompleteUriGestion_buildsGestionScopedOperationIri() {
        assertThat(uriUtils.getCompleteUriGestion("operation", "s1250"))
                .isEqualTo("http://bauhaus/operations/operation/s1250");
    }

    @Test
    void getCompleteUriPublication_buildsPublicationScopedOperationIri() {
        assertThat(uriUtils.getCompleteUriPublication("operation", "s1250"))
                .isEqualTo("http://id.insee.fr/operations/operation/s1250");
    }
}
