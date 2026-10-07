package fr.insee.rmes.modules.operation.operations.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConfiguredOperationIrisAdapterTest {

    private final ConfiguredOperationIrisAdapter adapter =
            new ConfiguredOperationIrisAdapter("http://id.insee.fr/", "http://bauhaus/", "operations/operation");

    /** L'init DDI écrit l'IRI de publication, la création depuis l'IHM celle de gestion. */
    @Test
    void namesTheOperationUnderItsPublicationAndItsManagementIri() {
        assertThat(adapter.irisOf("s1268"))
                .containsExactly(
                        "http://id.insee.fr/operations/operation/s1268", "http://bauhaus/operations/operation/s1268");
    }
}
