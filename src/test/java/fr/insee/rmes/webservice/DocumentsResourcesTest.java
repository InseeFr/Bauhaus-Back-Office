package fr.insee.rmes.webservice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(properties={
        "fr.insee.rmes.bauhaus.env=PROD",

}, controllers = DocumentsResources.class)
@AutoConfigureMockMvc
@Import({AuthorizeMethodDecider.class, Config.class})
class DocumentsResourcesTest {

    @MockBean
    private DocumentsService documentsService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void deleteDocument_testNoAuthCantDelete(@Autowired MockMvc mvc) throws Exception {
        String documentId="1";
        when(documentsService.deleteDocument(documentId)).thenReturn(OK);

        mvc.perform(delete("/document/"+documentId))
                .andExpect(status().isForbidden());
    }


    @Test
    void deleteDocument_testAdminCanDelete(@Autowired MockMvc mvc) throws Exception {
        String documentId="1";
        Date issuedAt=new Date();
        Date expiresAT=Date.from((new Date()).toInstant().plusSeconds(100));
        String token=JWT.create()
                .withAudience("k8s-onboarding", "account")
                .withExpiresAt(expiresAT)
                .withIssuedAt(issuedAt)
                .withIssuer("https://keycloak.insee/auth/realms/interne")
                .withSubject("f:blabla:Toto")
                .withKeyId("blabla")
                .withClaim("typ", "Bearer")
                .withClaim("realm_access", "{\"roles\":[\"Administrateur_RMESGNCS\"]}")
                .withClaim("timbre", "DR59-SNDI")
                .withClaim("preferred_username", "bibi")
                .sign(Algorithm.none());
        Jwt jwt=new Jwt(token,issuedAt.toInstant(),expiresAT.toInstant(), Map.of("alg", "RS256","typ", "JWT"), Map.of("realm_access", "{\"roles\":[\"Administrateur_RMESGNCS\"]}") );
        //var token=JWT.decode("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJqYy1MUEVCdlRFYnV2V1d4aV9jT2NtLU5uUWI5Rk01X09uRkFMN0JrWndnIn0.eyJleHAiOjE2ODYyNDI3MTUsImlhdCI6MTY4NjIzMTkxNSwiYXV0aF90aW1lIjoxNjg2MjMxOTE1LCJqdGkiOiI1YWVmMTM5Zi05ZTVkLTRhM2MtOWMxOC1jNmNjMDA4YWMxYjQiLCJpc3MiOiJodHRwczovL2F1dGguaW5zZWUudGVzdC9hdXRoL3JlYWxtcy9hZ2VudHMtaW5zZWUtaW50ZXJuZSIsImF1ZCI6WyJrOHMtb25ib2FyZGluZyIsImFjY291bnQiXSwic3ViIjoiZjpmYzMwN2I3MS01ZTcwLTQxYjktOGVlMi1lNzViMmJlMDcxMDQ6WFJNRlVYIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibG9jYWxob3N0LWZyb250ZW5kIiwibm9uY2UiOiIyNjFlOWQ2OC0zZTlkLTRkMDgtODYxOS0wODE0ODYzOWVjNTUiLCJzZXNzaW9uX3N0YXRlIjoiODVhMjhkYzQtZjQzNS00ZTgyLTliOTgtZGQ0ZjQ2MjU0YTgyIiwiYWNyIjoiZWlkYXMxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkV4cGVydF9OYXRpb25hbF8gT0NUT1BVU1NFIiwiQVNJX0Zvcm11bCIsImRlZmF1bHQtcm9sZXMtYWdlbnRzLWluc2VlLWludGVybmUiLCJBZG1pbmlzdHJhdGV1cl9DUkFCRSIsImdlc3Rpb25uYWlyZWxvY2FsX0NhcGkzRyIsIkNPREVVUi1FRUNfUmVwcmlzZUVtcGxvaXNHZW5lcmlxdWUiLCJBZG1pbmlzdHJhdGV1cl9STUVTR05DUyIsIkNPREVVUi1GUVBfUmVwcmlzZUZvcm1hdGlvbnNHZW5lcmlxdWUiLCJSZXNwTmF0RW5xdWV0ZV9FRUMzIiwiRVhQRVJUX05hdXRpbGUiLCJDT05TVUxUQVRJT04tRlFQX1JlcHJpc2VGb3JtYXRpb25zR2VuZXJpcXVlIiwiQXBpLWNyYWJlX05hdXRpbGUiLCJVdGlsaXNhdGV1cnNfQ29sdHJhbmUtUGlsb3RhZ2UiLCJjcmFmdF9LOFMtUE9DIiwibWFpbnRlbmljaWVuX0NhcGkzRyIsIkFTSV9HREIiLCJDT0RFVVItRUVDX1JlcHJpc2VGb3JtYXRpb25zR2VuZXJpcXVlIiwib2ZmbGluZV9hY2Nlc3MiLCJyZW5mb3J0c19DaWdhbCIsIlJPTEVfQURNSU5fR0RCIiwiazhzLXBvYy1wb2d1ZXMtZW5vIiwidW1hX2F1dGhvcml6YXRpb24iLCJSZXNwQ29sbGVjdGVOYXRfRUVDMyJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIGluc2VlUm9sZUFwcGxpY2F0aWYgaW5zZWVUaW1icmUgcHJvZmlsZSIsInNpZCI6Ijg1YTI4ZGM0LWY0MzUtNGU4Mi05Yjk4LWRkNGY0NjI1NGE4MiIsInRpbWJyZSI6IkRSNTktU05ESSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibWF0cmljdWxlIjoiMDAwMDAyMDAyNTU0IiwibmFtZSI6IkZhYnJpY2UgQmlib25uZSIsInByZWZlcnJlZF91c2VybmFtZSI6InhybWZ1eCIsImdpdmVuX25hbWUiOiJGYWJyaWNlIiwieHdpa2lfZ3JvdXBzIjpbIkV4cGVydF9OYXRpb25hbF8gT0NUT1BVU1NFIiwiQVNJX0Zvcm11bCIsImRlZmF1bHQtcm9sZXMtYWdlbnRzLWluc2VlLWludGVybmUiLCJBZG1pbmlzdHJhdGV1cl9DUkFCRSIsImdlc3Rpb25uYWlyZWxvY2FsX0NhcGkzRyIsIkNPREVVUi1FRUNfUmVwcmlzZUVtcGxvaXNHZW5lcmlxdWUiLCJBZG1pbmlzdHJhdGV1cl9STUVTR05DUyIsIkNPREVVUi1GUVBfUmVwcmlzZUZvcm1hdGlvbnNHZW5lcmlxdWUiLCJSZXNwTmF0RW5xdWV0ZV9FRUMzIiwiRVhQRVJUX05hdXRpbGUiLCJDT05TVUxUQVRJT04tRlFQX1JlcHJpc2VGb3JtYXRpb25zR2VuZXJpcXVlIiwiQXBpLWNyYWJlX05hdXRpbGUiLCJVdGlsaXNhdGV1cnNfQ29sdHJhbmUtUGlsb3RhZ2UiLCJjcmFmdF9LOFMtUE9DIiwibWFpbnRlbmljaWVuX0NhcGkzRyIsIkFTSV9HREIiLCJDT0RFVVItRUVDX1JlcHJpc2VGb3JtYXRpb25zR2VuZXJpcXVlIiwib2ZmbGluZV9hY2Nlc3MiLCJyZW5mb3J0c19DaWdhbCIsIlJPTEVfQURNSU5fR0RCIiwiazhzLXBvYy1wb2d1ZXMtZW5vIiwidW1hX2F1dGhvcml6YXRpb24iLCJSZXNwQ29sbGVjdGVOYXRfRUVDMyJdLCJmYW1pbHlfbmFtZSI6IkJpYm9ubmUiLCJlbWFpbCI6ImZhYnJpY2UuYmlib25uZUBpbnNlZS5mciJ9.TDAtAR-Yomu7tfDXFnP_A0LXgaiG7EZtC3VocqS8K4CLI0o3xjm4oE5mk5eS1n1Sv1pTarhk07qFtGb5PZQddowlJ9WHYndIHms6bbmmo5prk8zCGUPC9uJbIoIqqqW9xdOVVHMhZZcn5L6uzP4-jg4ELGeV3952Btu5LCno64KcaaU_Y5wWm7RD6GA1rLEYRonUvNVPotgfk-Y0oJNwI-qNJhz3hPDByU63YmeoEKhHHXeznYjvQNM-YbctNKeDDSCIF8hyEBBz2w5jVRGol5HQ23shQabr5WONlAvk7f2PxjRuKmWLtXBPOOm5tjl8ne7GnQfNKifafgnRLMKEvg");
        when(documentsService.deleteDocument(documentId)).thenReturn(OK);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);

        mvc.perform(delete("/document/"+documentId).header("Authorization", "bearer "+token))
                .andExpect(status().isOk());
    }

}