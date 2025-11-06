package fr.insee.rmes.modules.concepts.collections.webservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = CollectionsResourcesDisabledIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.activeModules=operations")
class CollectionsResourcesDisabledIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(CollectionsResources.class)
    static class TestConfiguration {
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldNotLoadControllerWhenConceptsModuleIsNotActive() {
        assertThrows(NoSuchBeanDefinitionException.class,
            () -> applicationContext.getBean(CollectionsResources.class),
            "CollectionsResources should not be loaded when activeModules does not contain 'concepts'");
    }
}
