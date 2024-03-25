package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.webservice.distribution.DistributionResources;
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
import static fr.insee.rmes.model.ValidationStatus.UNPUBLISHED;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DistributionResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=datasets"}
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        BauhausMethodSecurityExpressionHandler.class})
public class TestDistributionsResourcesEnvProd {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private DatasetService datasetService;
    @MockBean
    private DistributionService distributionService;
    @MockBean
    StampAuthorizationChecker stampAuthorizationChecker;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    int distributionId =10;
    ValidationStatus status= UNPUBLISHED;

    @Test
    void shouldGetDistributionsWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());

        mvc.perform(get("/distribution").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDistributionsWhenAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        mvc.perform(get("/distribution").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDistributionsWhenDatasetContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));

        mvc.perform(get("/distribution").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDistributionWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());

        mvc.perform(get("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDistributionWhenAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        mvc.perform(get("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDistributionWhenDatasetContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));

        mvc.perform(get("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void shouldCreateADistributionIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(post("/distribution/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateADistributionIfDatasetContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(post("/distribution/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldNotCreateADistribution() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(post("/distribution/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateADistributionIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateADistributionIfDatasetContributorBasedOnStamp() throws Exception {
        when(stampAuthorizationChecker.isDistributionManagerWithStamp(String.valueOf(distributionId),timbre)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(put("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotUpdateADistributionIfDatasetContributorWithoutStamp() throws Exception {
        when(stampAuthorizationChecker.isDistributionManagerWithStamp(String.valueOf(distributionId),timbre)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(put("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotUpdateADistribution() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(put("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPublishADistributionIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/distribution/" + distributionId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPublishADistributionIfDatasetContributorBasedOnStamp() throws Exception {
        when(stampAuthorizationChecker.isDistributionManagerWithStamp(String.valueOf(distributionId),timbre)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(put("/distribution/" + distributionId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotPublishADistributionIfDatasetContributorWithoutStamp() throws Exception {
        when(stampAuthorizationChecker.isDistributionManagerWithStamp(String.valueOf(distributionId),timbre)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(put("/distribution/" + distributionId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotPublisheADistribution() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(put("/distribution/" + distributionId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteADistributionIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(delete("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteADistributionIfDatasetContributorBasedOnStamp() throws Exception {
        when(stampAuthorizationChecker.isDistributionManagerWithStamp(String.valueOf(distributionId),timbre)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(delete("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void shouldNotDeleteADistributionIfDatasetContributorWithoutStamp() throws Exception {
        when(stampAuthorizationChecker.isDistributionManagerWithStamp(String.valueOf(distributionId),timbre)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(delete("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotDeleteADistribution() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(delete("/distribution/" + distributionId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
