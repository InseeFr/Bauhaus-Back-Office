package fr.insee.rmes.bauhaus_services.keycloak;

import fr.insee.rmes.config.keycloak.KeycloakServerZoneConfiguration;
import fr.insee.rmes.config.keycloak.ServerZone;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.stubs.KeycloakServicesStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RestClientTest
@Import({KeycloakServicesStub.class, KeycloakServerZoneConfiguration.class})
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.keycloak.client.secret = XXX",
        "fr.insee.rmes.bauhaus.keycloak.client.id = XXX",
        "fr.insee.rmes.bauhaus.auth-server-url = keycloak.interne",
        "fr.insee.rmes.bauhaus.keycloak.client.dmz.secret = XXX",
        "fr.insee.rmes.bauhaus.keycloak.client.dmz.id = XXX",
        "fr.insee.rmes.bauhaus.dmz.auth-server-url = keycloak.dmz",
        "fr.insee.rmes.bauhaus.keycloak-configuration.zoneByServers.[serverinterne.insee.fr].zone=interne",
        "fr.insee.rmes.bauhaus.keycloak-configuration.zoneByServers.[servergestion.insee.fr].zone=interne",
        "fr.insee.rmes.bauhaus.keycloak-configuration.zoneByServers.[serverdmz.insee.fr].zone=dmz",
})
class KeycloakServicesTest {

    @Mock
    private RestTemplate testRestTemplate;

    @Autowired
    private KeycloakServicesStub keycloakServices;

    @Test
    void nowPlus1Second() {
        var keycloakServerZoneConfiguration =  new KeycloakServerZoneConfiguration();
        keycloakServerZoneConfiguration.setZoneByServers(Map.of());
        var keycloakServices = new KeycloakServices("s", "i", "s",
                "d", "di", "dk",keycloakServerZoneConfiguration);
        var start= new Date();
        var actual=keycloakServices.nowPlus1Second();
        var nowPlus1= Date.from(start.toInstant().plusSeconds(1));
        var nowPlus10= Date.from(start.toInstant().plusSeconds(10));
        assertFalse(actual.before(nowPlus1));
        assertTrue(actual.before(nowPlus10));
    }

    @BeforeEach
    void given(){
        Token token = new Token() {
            @Override
            public String getAccessToken() {
                return "token";
            }
        };
        when(this.testRestTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Token.class))).thenReturn(token);
        this.keycloakServices.setRestTemplate(this.testRestTemplate);
    }

    @Test
    void getKeycloakAccessToken_tokenReferInterneForInterne() throws RmesException {

        this.keycloakServices.getKeycloakAccessToken("http://serverinterne.insee.fr");
        Mockito.verify(testRestTemplate).postForObject(eq("keycloak.interne/protocol/openid-connect/token"), any(HttpEntity.class), eq(Token.class));
    }

    @Test
    void getKeycloakAccessToken_tokenReferInterneForGestion() throws RmesException {
        this.keycloakServices.getKeycloakAccessToken("http://servergestion.insee.fr");
        Mockito.verify(testRestTemplate).postForObject(eq("keycloak.interne/protocol/openid-connect/token"), any(HttpEntity.class), eq(Token.class));
    }

    @Test
    void getKeycloakAccessToken_tokenReferExterneForExterne() throws RmesException {
        this.keycloakServices.getKeycloakAccessToken("http://serverdmz.insee.fr");
        Mockito.verify(testRestTemplate).postForObject(eq("keycloak.dmz/protocol/openid-connect/token"), any(HttpEntity.class), eq(Token.class));
    }

    @Test
    void shouldAnswerThatTokenIsNotValid(){
        String firstToken = null;
        String secondToken = "eyJhbGciOiJSUzI1N1rQ";
        String thirdToken = "-/*-";
        String fourthToken = "11";
        List<Boolean> actual = List.of(keycloakServices.isTokenValid(firstToken),
                keycloakServices.isTokenValid(secondToken),
                keycloakServices.isTokenValid(thirdToken),
                keycloakServices.isTokenValid(fourthToken)
                );
        List<Boolean> expected = List.of(false,false,false,false);
        assertEquals(expected,actual);
    }

    @Test
    void shouldFindZoneFromUrlAndStoreIt(){
        ServerZone firstServerZone = new ServerZone();
        ServerZone secondServerZone = new ServerZone();

        firstServerZone.setZone("DMZ");
        secondServerZone.setZone("INTERNE");

        Map<String, ServerZone> rdfserver = Map.of("FIRST",firstServerZone,"SECOND",secondServerZone);
        KeycloakServerZoneConfiguration keycloakServerZoneConfiguration = new KeycloakServerZoneConfiguration(rdfserver);
        KeycloakServices keycloakServicesExamples = new KeycloakServices("secret","clientID","serverKeycloak","secretDmz","clientDmzId","serverKeycloakDmz",keycloakServerZoneConfiguration);

        ServerZone firstResearch = keycloakServicesExamples.findZoneFromUrlAndStoreIt("FIRST");
        ServerZone secondResearch = keycloakServicesExamples.findZoneFromUrlAndStoreIt("SECOND");
        ServerZone thirdResearch = keycloakServicesExamples.findZoneFromUrlAndStoreIt("non-existent URL");

        List<ServerZone> actual = List.of(firstResearch,secondResearch,thirdResearch);
        List<ServerZone> expected = List.of( firstServerZone,secondServerZone,secondServerZone);
        assertEquals(expected.toString(),actual.toString());

        }
}