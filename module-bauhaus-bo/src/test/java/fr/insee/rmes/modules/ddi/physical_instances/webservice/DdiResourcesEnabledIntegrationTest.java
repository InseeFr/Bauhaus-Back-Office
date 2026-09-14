package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = DdiResourcesEnabledIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.modules.ddi.enabled=true")
class DdiResourcesEnabledIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(DdiResources.class)
    static class TestConfiguration {}

    @MockitoBean
    private DDIService ddiService;

    @MockitoBean
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    @MockitoBean
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @MockitoBean
    private DDIItemConvertService ddiItemConvertService;

    @MockitoBean
    private UserProvider userProvider;

    @MockitoBean
    private RbacFetcher rbacFetcher;

    @MockitoBean
    private BauhausUriBuilder bauhausUriBuilder;

    @MockitoBean
    private Ddi4SchemaService ddi4SchemaService;

    @Autowired
    private DdiResources ddiResources;

    @Test
    void shouldLoadControllerWhenDdiModuleIsActive() {
        assertNotNull(ddiResources, "DdiResources should be loaded when activeModules contains 'ddi'");
    }
}
