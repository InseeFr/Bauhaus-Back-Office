package fr.insee.rmes.colectica.mock.webservice;

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

@SpringBootTest(classes = ColecticaMockControllerDisabledIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.colectica.mock-server-enabled=false")
class ColecticaMockControllerDisabledIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(ColecticaMockResources.class)
    static class TestConfiguration {
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldNotLoadControllerWhenDisabled() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            applicationContext.getBean(ColecticaMockResources.class);
        });
    }
}