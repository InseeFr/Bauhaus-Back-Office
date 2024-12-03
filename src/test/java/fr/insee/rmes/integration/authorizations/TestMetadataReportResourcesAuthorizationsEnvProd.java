package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.operations.MetadataReportResources;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MetadataReportResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=operations"}
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class})
class TestMetadataReportResourcesAuthorizationsEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OperationsDocumentationsService documentationsService;

    @MockBean
    private OperationsService operationsService;

    @MockBean
    StampAuthorizationChecker stampAuthorizationChecker;

    @MockBean
    private JwtDecoder jwtDecoder;

    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";

    @Test
    void testGetMSDJson() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

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
    void testGetMSDXml() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

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
    void testGetMSDJsonRmesException() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        when(documentationsService.getMSDJson()).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Detailed error message"));

        mvc.perform(get("/operations/metadataStructureDefinition")
                        .header("Authorization", "Bearer toto")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetMSDXmlRmesException() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        when(documentationsService.getMSD()).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Detailed error message"));

        mvc.perform(get("/operations/metadataStructureDefinition")
                        .header("Authorization", "Bearer toto")
                        .header("Accept", MediaType.APPLICATION_XML_VALUE))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void testGetMetadataAttribute() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        String id = "testId";
        String jsonResponse = "{\"key\":\"value\"}";

        when(documentationsService.getMetadataAttribute(id)).thenReturn(jsonResponse);

        mvc.perform(get("/operations/metadataAttribute/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void testGetMetadataAttributeRmesException() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        String id = "testId";

        when(documentationsService.getMetadataAttribute(id)).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Detailed error message"));

        mvc.perform(get("/operations/metadataAttribute/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetMetadataAttributes() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        String jsonResponse = "{\"key\":\"value\"}";

        when(documentationsService.getMetadataAttributes()).thenReturn(jsonResponse);

        mvc.perform(get("/operations/metadataAttributes")
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void testGetMetadataAttributesRmesException() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        when(documentationsService.getMetadataAttributes()).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Detailed error message"));

        mvc.perform(get("/operations/metadataAttributes")
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetMetadataReport() throws Exception {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

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
    void testGetMetadataReportRmesException() throws Exception {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        when(documentationsService.getMetadataReport(id)).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Detailed error message"));

        mvc.perform(get("/operations/metadataReport/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void testGetMetadataReportDefaultValue() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

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
    void testGetFullSimsJson() throws Exception {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

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
    void testGetFullSimsXml() throws Exception {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

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
    void testGetFullSimsJsonRmesException() throws Exception {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        when(documentationsService.getFullSimsForJson(id)).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Detailed error message"));

        mvc.perform(get("/operations/metadataReport/fullSims/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetFullSimsXmlRmesException() throws Exception {
        String id = "1234";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        when(documentationsService.getFullSimsForXml(id)).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Detailed error message"));

        mvc.perform(get("/operations/metadataReport/fullSims/{id}", id)
                        .header("Authorization", "Bearer toto")
                        .header("Accept", MediaType.APPLICATION_XML_VALUE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetSimsExport() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

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
    void testGetSimsExport_DefaultValues() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

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
    void postMetadataReportAdmin_OK() throws Exception {
        when(documentationsService.createMetadataReport(anyString())).thenReturn("{}");
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
    void postMetadataReportContributor_OK() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
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
