package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ColecticaConfigurationTest.TestConfiguration.class)
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.colectica.server.baseUrl=http://localhost:8082",
        "fr.insee.rmes.bauhaus.colectica.server.apiPath=/api/v1/",
        "fr.insee.rmes.bauhaus.colectica.server.username=test-user",
        "fr.insee.rmes.bauhaus.colectica.server.password=test-password",
        "fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].agency-id=fr.insee",
        "fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].identifier=fc65a527-a04b-4505-85de-0a181e54dbad",
        "fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].version=1",
        "fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[1].agency-id=other.agency",
        "fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[1].identifier=another-uuid",
        "fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[1].version=2",
})
class ColecticaConfigurationTest {

    @Autowired
    private ColecticaConfiguration colecticaConfiguration;

    @Test
    void shouldLoadConfigurationPropertiesForPrimaryInstance() {

        // Verify primary instance configuration
        assertEquals("http://localhost:8082", colecticaConfiguration.server().baseUrl());
        assertEquals("/api/v1/", colecticaConfiguration.server().apiPath());
        assertEquals("http://localhost:8082", colecticaConfiguration.server().baseServerUrl());
        assertEquals("http://localhost:8082/api/v1/", colecticaConfiguration.server().baseApiUrl());
        assertEquals("test-user", colecticaConfiguration.server().username());
        assertEquals("test-password", colecticaConfiguration.server().password());
    }

    @Test
    void shouldLoadMutualizedCodesListsConfiguration() {
        // Verify mutualized codes lists configuration
        assertNotNull(colecticaConfiguration.mutualizedCodesLists());
        assertEquals(2, colecticaConfiguration.mutualizedCodesLists().size());

        // First entry
        ColecticaConfiguration.MutualizedCodeListEntry firstEntry = colecticaConfiguration.mutualizedCodesLists().get(0);
        assertEquals("fr.insee", firstEntry.agencyId());
        assertEquals("fc65a527-a04b-4505-85de-0a181e54dbad", firstEntry.identifier());
        assertEquals(1, firstEntry.version());

        // Second entry
        ColecticaConfiguration.MutualizedCodeListEntry secondEntry = colecticaConfiguration.mutualizedCodesLists().get(1);
        assertEquals("other.agency", secondEntry.agencyId());
        assertEquals("another-uuid", secondEntry.identifier());
        assertEquals(2, secondEntry.version());
    }

    @EnableConfigurationProperties(ColecticaConfiguration.class)
    static class TestConfiguration {
    }
}