package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = DdiResourcesDisabledIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.activeModules=concepts")
class DdiResourcesDisabledIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(DdiResources.class)
    static class TestConfiguration {
    }

    @MockitoBean
    private DDIService ddiService;

    @MockitoBean
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    @MockitoBean
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldNotLoadControllerWhenDdiModuleIsNotActive() {
        assertThrows(NoSuchBeanDefinitionException.class,
            () -> applicationContext.getBean(DdiResources.class),
            "DdiResources should not be loaded when activeModules does not contain 'ddi'");
    }
}