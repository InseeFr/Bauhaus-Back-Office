package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = CollectionsResourcesEnabledIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.activeModules=concepts")
class CollectionsResourcesEnabledIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(CollectionsResources.class)
    static class TestConfiguration {
    }

    @MockBean
    private CollectionsService collectionsService;

    @Autowired
    private CollectionsResources collectionsResources;

    @Test
    void should_load_controller_when_concepts_module_is_active() {
        assertNotNull(collectionsResources, "CollectionsResources should be loaded when activeModules contains 'concepts'");
    }
}
