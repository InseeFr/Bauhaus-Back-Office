package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import fr.insee.rmes.modules.operations.msd.webservice.MetadataReportResources;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import fr.insee.rmes.utils.XMLUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MetadataReportResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=operations",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        MetadataReportResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class TestMetadataReportResourcesAuthorizationsEnvProd extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private OperationsDocumentationsService documentationsService;

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private DocumentationService documentationService;



    @Test
    void testGetMSDJson() throws Exception, MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        String jsonResponse = "{\"key\":\"value\"}";

        when(documentationsService.getMSDJson()).thenReturn(jsonResponse);

        mvc.perform(get("/operations/metadataStructureDefinition")
                        .header("Authorization", "Bearer toto")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void testGetMSDXml() throws Exception, MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        MSD msd = new MSD();
        String xmlResponse = "<MSD><key>value</key></MSD>";

        when(documentationsService.getMSD()).thenReturn(msd);
        try (MockedStatic<XMLUtils> mockedFactory = Mockito.mockStatic(XMLUtils.class)) {
            mockedFactory.when(() -> XMLUtils.produceResponse(msd, MediaType.APPLICATION_XML_VALUE)).thenReturn(xmlResponse);

            mvc.perform(get("/operations/metadataStructureDefinition")
                            .header("Authorization", "Bearer toto")
                            .header("Accept", MediaType.APPLICATION_XML_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/xml;charset=UTF-8"))
                    .andExpect(content().xml(xmlResponse));
        }
    }


    @Test
    void testGetMetadataReport() throws Exception, MissingUserInformationException {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        String jsonResponse = "{\"key\":\"value\"}";

        when(documentationsService.getMetadataReport(id)).thenReturn(jsonResponse);

        mvc.perform(get("/operations/metadataReport/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse));
    }


    @Test
    void testGetMetadataReportDefaultValue() throws Exception, MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        String jsonResponse = "{\"key\":\"value\"}";

        when(documentationsService.getMetadataReportDefaultValue()).thenReturn(jsonResponse);

        mvc.perform(get("/operations/metadataReport/default")
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void testGetFullSimsJson() throws Exception, MissingUserInformationException {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        String jsonResponse = "{\"key\":\"value\"}";

        when(documentationsService.getFullSimsForJson(id)).thenReturn(jsonResponse);

        mvc.perform(get("/operations/metadataReport/fullSims/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void testGetFullSimsXml() throws Exception, MissingUserInformationException {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        Documentation documentation = new Documentation();
        String xmlResponse = "<MSD><key>value</key></MSD>";

        when(documentationsService.getFullSimsForXml(id)).thenReturn(documentation);
        try (MockedStatic<XMLUtils> mockedFactory = Mockito.mockStatic(XMLUtils.class)) {
            mockedFactory.when(() -> XMLUtils.produceResponse(documentation, MediaType.APPLICATION_XML_VALUE)).thenReturn(xmlResponse);

            mvc.perform(get("/operations/metadataReport/fullSims/{id}", id)
                            .header("Authorization", "Bearer toto")
                            .header("Accept", MediaType.APPLICATION_XML_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/xml;charset=UTF-8"))
                    .andExpect(content().xml(xmlResponse));
        }
    }


    @Test
    void testGetSimsExport() throws Exception, MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = true;
        boolean document = true;
        Resource resource = new ByteArrayResource("Mocked Document Content".getBytes());

        when(documentationsService.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document))
                .thenReturn(ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource));

        mvc.perform(get("/operations/metadataReport/export/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .param("emptyMas", String.valueOf(includeEmptyMas))
                        .param("lg1", String.valueOf(lg1))
                        .param("lg2", String.valueOf(lg2))
                        .param("document", String.valueOf(document))
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string("Mocked Document Content"));
    }

    @Test
    void testGetSimsExport_DefaultValues() throws Exception, MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        String id = "1234";
        Resource resource = new ByteArrayResource("Mocked Document Content".getBytes());

        when(documentationsService.exportMetadataReport(id, true, true, true, true))
                .thenReturn(ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource));

        mvc.perform(get("/operations/metadataReport/export/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string("Mocked Document Content"));
    }

    @Test
    void postMetadataReportAdmin_OK() throws Exception, MissingUserInformationException {
        when(documentationsService.createMetadataReport(anyString())).thenReturn("{}");
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rubrics": [],
                                    "idSeries": "",
                                    "labelLg2": "Quality report: Monthly tendency survey in industry 2023",
                                    "labelLg1": "Rapport qualité : Enquête mensuelle de conjoncture dans l'industrie 2023",
                                    "idOperation": "s2098",
                                    "created": "2022-12-12T14:25:53.275014",
                                    "idIndicator": "",
                                    "id": "2079",
                                    "updated": "2023-10-02T19:14:12.465548575",
                                    "validationState": "Validated"
                                  }
                                """))
                .andExpect(status().isOk());
    }


    @Test
    void postMetadataReportContributor_OK() throws Exception, MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        when(documentationsService.createMetadataReport(anyString())).thenReturn("{}");
        mvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rubrics": [],
                                    "idSeries": "",
                                    "labelLg2": "Quality report: Monthly tendency survey in industry 2023",
                                    "labelLg1": "Rapport qualité : Enquête mensuelle de conjoncture dans l'industrie 2023",
                                    "idOperation": "s2098",
                                    "created": "2022-12-12T14:25:53.275014",
                                    "idIndicator": "",
                                    "id": "2079",
                                    "updated": "2023-10-02T19:14:12.465548575",
                                    "validationState": "Validated"
                                  }
                                """))
                .andExpect(status().isOk());
    }


}
