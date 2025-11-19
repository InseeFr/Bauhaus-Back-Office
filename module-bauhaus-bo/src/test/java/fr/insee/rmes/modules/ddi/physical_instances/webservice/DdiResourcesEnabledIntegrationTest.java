package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = DdiResourcesEnabledIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.activeModules=ddi")
class DdiResourcesEnabledIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(DdiResources.class)
    static class TestConfiguration {
    }

    @MockBean
    private DDIService ddiService;

    @MockBean
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    @MockBean
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @Autowired
    private DdiResources ddiResources;

    @Test
    void shouldLoadControllerWhenDdiModuleIsActive() {
        assertNotNull(ddiResources, "DdiResources should be loaded when activeModules contains 'ddi'");
    }
}