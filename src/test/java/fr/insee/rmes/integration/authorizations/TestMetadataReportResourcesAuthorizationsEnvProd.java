package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.webservice.operations.MetadataReportResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MetadataReportResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE"}
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
    void postMetadataReportAdmin_OK() throws Exception {
        when(documentationsService.createMetadataReport(anyString())).thenReturn("{}");
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
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
