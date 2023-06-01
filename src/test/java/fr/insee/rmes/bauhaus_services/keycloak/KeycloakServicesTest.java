package fr.insee.rmes.bauhaus_services.keycloak;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakServicesTest {

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
}