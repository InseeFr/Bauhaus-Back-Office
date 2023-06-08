package fr.insee.rmes.webservice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Date;
import java.util.Map;

import static fr.insee.rmes.webservice.DocumentsResourcesTest.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(properties={"fr.insee.rmes.bauhaus.env=PROD", STAMP_CLAIM_PROPERTY, ROLE_CLAIM_PROPERTY, ID_CLAIM_PROPERTY},
        controllers = DocumentsResources.class)
@AutoConfigureMockMvc
@Import({AuthorizeMethodDecider.class, Config.class})
class DocumentsResourcesTest {

    public static final String STAMP_CLAIM="timbre";
    public static final String STAMP_CLAIM_PROPERTY="jwt.stamp-claim="+STAMP_CLAIM;
    private static final String ROLE_CLAIM = "realm_access";
    public static final String ROLE_CLAIM_PROPERTY="jwt.role-claim="+ROLE_CLAIM;
    private static final String ID_CLAIM = "preferred_username";
    public static final String ID_CLAIM_PROPERTY="jwt.id-claim="+ID_CLAIM;

    @MockBean
    private DocumentsService documentsService;

    @MockBean
    private JwtDecoder jwtDecoder;
    @Test
    void deleteDocument_testNoAuthCantDelete(@Autowired MockMvc mvc) throws Exception {
        String documentId="1";
        when(documentsService.deleteDocument(documentId)).thenReturn(OK);

        mvc.perform(delete("/documents/document/"+documentId))
                .andExpect(status().isForbidden());
    }


    @Test
    void deleteDocument_testAdminCanDelete(@Autowired MockMvc mvc) throws Exception {
        String documentId="1";
        Date issuedAt=new Date();
        Date expiresAT=Date.from((new Date()).toInstant().plusSeconds(100));
        Jwt jwt=new Jwt("token",issuedAt.toInstant(),expiresAT.toInstant(),
                Map.of("alg", "RS256","typ", "JWT"),
                Map.of("realm_access", "{\"roles\":[\"Administrateur_RMESGNCS\"]}",
                        "timbre", "DR59-SNDI",
                        "preferred_username", "bibi"
                )
        );
        when(documentsService.deleteDocument(documentId)).thenReturn(OK);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);

        mvc.perform(delete("/documents/document/"+documentId).header("Authorization", "bearer token_blabla"))
                .andExpect(status().isOk());
    }

}