package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.accesscontrol.ResourceOwnershipByStampVerifier;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.webservice.dataset.DatasetResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DatasetResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "spring.config.additional-location=classpath:rbac-test.yml",
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "logging.level.org.springframework.web=DEBUG",
                "logging.level.fr.insee.rmes.external.services.rbac=DEBUG",
                "fr.insee.rmes.bauhaus.activeModules=datasets"}
)
@Import( ConfigurationForTestWithAuth.class)
class TestDatasetsResourcesEnvProd {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    DatasetService datasetService;
    @MockBean
    ResourceOwnershipByStampVerifier resourceOwnershipByStampVerifier;


    private static Dataset dataset;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    int datasetId=10;


    @CsvSource(textBlock = """
            #Method, resource, droits, Optional<Body>, HttpStatusCode, hasRightStamp
            GET, '/datasets', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}',, 200,
            GET, '/datasets', '{Utilisateur_RMESGNCS}',, 200,
            GET, '/datasets', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}',, 200,
            GET, '/datasets/10', '{Utilisateur_RMESGNCS}',, 200,
            GET, '/datasets/10', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}',, 200,
            GET, '/datasets/10', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}',, 200,
            GET, '/datasets/10/distributions', '{Utilisateur_RMESGNCS}',, 200,
            GET, '/datasets/10/distributions', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}',, 200,
            GET, '/datasets/10/distributions', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}',, 200,
            POST, '/datasets', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}','{"id": "1"}', 201,
            POST, '/datasets', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}','{"id": "1"}', 201,
            POST, '/datasets', '{Utilisateur_RMESGNCS}', '{"id": "1"}', 403,
            PUT, '/datasets/10', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}','{"id": "1"}', 200,
            PUT, '/datasets/10', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}','{"id": "1"}', 200, true
            PUT, '/datasets/10', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}','{"id": "1"}', 403, false
            PUT, '/datasets/10', '{Utilisateur_RMESGNCS}', '{"id": "1"}', 403,
            GET, '/datasets/archivageUnits', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}',, 200,
            GET, '/datasets/archivageUnits', '{Utilisateur_RMESGNCS}',, 200,
            GET, '/datasets/archivageUnits', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}',, 200,
            PATCH, '/datasets/10', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}','{"numObservations": 1}', 200,
            PATCH, '/datasets/10', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}','{"numObservations": 1}', 200, true
            PATCH, '/datasets/10', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}','{"numObservations": 1}', 403, false
            PATCH, '/datasets/10', '{Utilisateur_RMESGNCS}', '{"numObservations": 1}', 403,
            DELETE, '/datasets/10', '{Utilisateur_RMESGNCS}',, 403,
            DELETE, '/datasets/10', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}',, 200,
            DELETE, '/datasets/10', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}',, 200, true
            DELETE, '/datasets/10', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}',, 403, false
            PUT, '/datasets/10/validate', '{Utilisateur_RMESGNCS, Administrateur_RMESGNCS}',, 200,
            PUT, '/datasets/10/validate', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}',, 200, true
            PUT, '/datasets/10/validate', '{Utilisateur_RMESGNCS, Gestionnaire_jeu_donnees_RMESGNCS}',, 403, false
            PUT, '/datasets/10/validate', '{Utilisateur_RMESGNCS}', , 403,
            """)
    @ParameterizedTest(name = "{0} {1} for {2} => {4}")
    void shouldGetDatasets(String method, String getResource, String roles, String content, int expectedStatus, Boolean hasRightStamp) throws Exception {
        if (hasRightStamp!=null){
            when(resourceOwnershipByStampVerifier.isDatasetManagerWithStamp(String.valueOf(datasetId),new Stamp(timbre))).thenReturn(hasRightStamp);
        }
        configureJwtDecoderMock(jwtDecoder, idep, timbre, toList(roles));
        Function<String, MockHttpServletRequestBuilder> httpCall = switch(method){
            case "GET" -> MockMvcRequestBuilders::get;
            case "POST" -> MockMvcRequestBuilders::post;
            case "PUT" -> MockMvcRequestBuilders::put;
            case "DELETE" -> MockMvcRequestBuilders::delete;
            case "PATCH" -> MockMvcRequestBuilders::patch;
            default -> throw new IllegalArgumentException("Unknown method: " + method);
        };
        mvc.perform(httpCall.apply(getResource).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                        .content(content==null?new byte[0]:content.getBytes()))
                .andExpect(status().is(expectedStatus));
    }

    private List<String> toList(String roles) {
        return Arrays.stream(roles.substring(1,roles.length()-1).split(",")).map(String::trim).toList();
    }

}
