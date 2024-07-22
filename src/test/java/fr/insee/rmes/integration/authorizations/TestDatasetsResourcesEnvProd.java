package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.webservice.dataset.DatasetResources;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DatasetResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "logging.level.org.springframework.web=DEBUG",
                "fr.insee.rmes.bauhaus.activeModules=datasets"}
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        BauhausMethodSecurityExpressionHandler.class})
class TestDatasetsResourcesEnvProd {


    private static final Logger logger= LoggerFactory.getLogger(TestDatasetsResourcesEnvProd.class);

    @Autowired
    private MockMvc mvc;
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    StampAuthorizationChecker stampAuthorizationChecker;
    @MockBean
    DatasetService datasetService;

    private static Dataset dataset;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    int datasetId=10;



    @Test
    void shouldGetDatasetsWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());

        mvc.perform(get("/datasets").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDatasetsWhenAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        mvc.perform(get("/datasets").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDatasetsWhenDatasetContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));

        mvc.perform(get("/datasets").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDatasetWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());

        mvc.perform(get("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDatasetWhenAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        mvc.perform(get("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDatasetWhenDatasetContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));

        mvc.perform(get("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDistributionsWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());

        mvc.perform(get("/datasets/" + datasetId + "/distributions").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDistributionsWhenAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        mvc.perform(get("/datasets/" + datasetId + "/distributions").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDistributionsWhenDatasetContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));

        mvc.perform(get("/datasets/" + datasetId + "/distributions").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateADatasetIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(post("/datasets/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateADatasetIfDatasetContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(post("/datasets/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldNotCreateADataset() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(post("/datasets/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateADatasetIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateADatasetIfDatasetContributorBasedOnStamp() throws Exception {
        when(stampAuthorizationChecker.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(put("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotUpdateADatasetIfDatasetContributorWithoutStamp() throws Exception {
        when(stampAuthorizationChecker.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(put("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotUpdateADataset() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(put("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPublishADatasetIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/datasets/" + datasetId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPublishADatasetIfDatasetContributorBasedOnStamp() throws Exception {
        when(stampAuthorizationChecker.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(put("/datasets/" + datasetId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotPublishADatasetIfDatasetContributorWithoutStamp() throws Exception {
        when(stampAuthorizationChecker.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(put("/datasets/" + datasetId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotPublisheADataset() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(put("/datasets/" + datasetId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteADatasetIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        dataset=new Dataset();
        dataset.setValidationState("Unpublished");
        mvc.perform(delete("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteADatasetIfDatasetContributorBasedOnStamp() throws Exception {
        when(stampAuthorizationChecker.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        dataset=new Dataset();
        dataset.setValidationState("Unpublished");
        mvc.perform(delete("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void shouldNotDeleteADatasetIfDatasetContributorWithoutStamp() throws Exception {
        when(stampAuthorizationChecker.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(delete("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotDeleteADataset() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(delete("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPatchADatasetIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(patch("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"observationNumber\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPatchADatasetIfDatasetContributorBasedOnStamp() throws Exception {
        when(stampAuthorizationChecker.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(patch("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"observationNumber\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotPatchADatasetIfDatasetContributorWithoutStamp() throws Exception {
        when(stampAuthorizationChecker.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.DATASET_CONTRIBUTOR));
        mvc.perform(patch("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"observationNumber\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotPatchADataset() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(patch("/datasets/" + datasetId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"observationNumber\": 1}"))
                .andExpect(status().isForbidden());
    }


}
