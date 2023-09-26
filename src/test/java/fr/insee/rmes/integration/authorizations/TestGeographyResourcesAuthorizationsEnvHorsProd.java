package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.webservice.GeographyResources;
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
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GeographyResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=HORSPROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM
        }
)
@Import({Config.class, OpenIDConnectSecurityContext.class, DefaultSecurityContext.class})
class TestGeographyResourcesAuthorizationsEnvHorsProd {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GeographyService geographyService;

    @MockBean
    private JwtDecoder jwtDecoder;


    /*private String createToken() throws NoSuchAlgorithmException, JSONException {
        //var keyPair=KeyPairGenerator.getInstance("RSA").generateKeyPair();
        return JWT.create().withClaim(ID_CLAIM, idep)
                .withClaim(STAMP_CLAIM, timbre)
                .withClaim(ROLE_CLAIM, jsonRoles())
                //.sign(Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate()));
                .sign(Algorithm.none());
    }*/
    @Test
    void getTerritoriesAuthentified_ok() throws Exception {
        String idep = "xxxxux";
        String timbre = "XX59-YYY";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bidon"));

        mvc.perform(get("/geo/territories").header("Authorization", "Bearer toto")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTerritoriesUnauthentified_ok() throws Exception {
        mvc.perform(get("/territories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void putTerritoriesUnauthentified_ok() {
        fail("PUT /territories unauthentified -> ok");
    }

    @Test
    void putTerritoriesNoAdmin_ok() {
        fail("PUT /territories no_admin -> ok");
    }

    @Test
    void putTerritoriesAdmin_ok() {
        fail("PUT /territories admin -> OK");
    }

    @Test
    void optionCreateTerritoriesUnauthentified_ok() {
        fail("OPTIONS /territories unauthentified -> OK");
    }

    @Test
    void optionCreateTerritoriesNoAdmin_ok() {
        fail("OPTIONS /territories no_admin -> OK");
    }

}
