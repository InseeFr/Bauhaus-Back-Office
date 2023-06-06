package fr.insee.rmes.bauhaus_services.keycloak;

import fr.insee.rmes.config.keycloak.KeycloakServerZoneConfiguration;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.stubs.KeycloakServicesStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RestClientTest
@Import({KeycloakServicesStub.class, KeycloakServerZoneConfiguration.class})
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.keycloak.client.secret = SECRET
        "fr.insee.rmes.bauhaus.keycloak.client.id = XXX",
        "fr.insee.rmes.bauhaus.auth-server-url = SECRET
        "fr.insee.rmes.bauhaus.keycloak.client.dmz.secret = SECRET
        "fr.insee.rmes.bauhaus.keycloak.client.dmz.id = XXX",
        "fr.insee.rmes.bauhaus.dmz.auth-server-url = SECRET
        "fr.insee.rmes.bauhaus.keycloak-configuration.zoneByServers.[serverinterne.insee.fr].zone=interne",
        "fr.insee.rmes.bauhaus.keycloak-configuration.zoneByServers.[servergestion.insee.fr].zone=interne",
        "fr.insee.rmes.bauhaus.keycloak-configuration.zoneByServers.[serverdmz.insee.fr].zone=dmz",
})
class KeycloakServicesTest {

    @Mock
    private RestTemplate testRestTemplate;

    @Autowired
    private KeycloakServicesStub keycloakServices;
    private Token token;

    @Test
    void nowPlus1Second() {
        var keycloakServices = new KeycloakServices();
        var start= new Date();
        var actual=keycloakServices.nowPlus1Second();
        var nowPlus1= Date.from(start.toInstant().plusSeconds(1));
        var nowPlus10= Date.from(start.toInstant().plusSeconds(10));
        assertFalse(actual.before(nowPlus1));
        assertTrue(actual.before(nowPlus10));
    }

    @BeforeEach
    void given(){
        this.token=new Token(){
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

}