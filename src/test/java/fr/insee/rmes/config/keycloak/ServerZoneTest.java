package fr.insee.rmes.config.keycloak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerZoneTest {

    private ServerZone serverZone;

    @BeforeEach
    void setUp() {
        serverZone = new ServerZone();
    }

    @Test
    void testSetZone_withValidZone() {
        String validZone = "INTERNE";

        serverZone.setZone(validZone);

        assertEquals(ServerZone.Zone.INTERNE, serverZone.zone());
    }

    @Test
    void testSetZone_withInvalidZone_logsWarningAndSetsDefaultZone() {
        String invalidZone = "INVALID_ZONE";

        serverZone.setZone(invalidZone);

        assertEquals(ServerZone.Zone.defaultZone(), serverZone.zone());
    }

    @Test
    void testConstructor_withValidZone() {
        String validZone = "DMZ";

        ServerZone serverZone = new ServerZone(validZone);

        assertEquals(ServerZone.Zone.DMZ, serverZone.zone());
    }

    @Test
    void testConstructor_withInvalidZone_logsWarningAndSetsDefaultZone() {
        String invalidZone = "UNKNOWN";

        ServerZone serverZone = new ServerZone(invalidZone);

        assertEquals(ServerZone.Zone.defaultZone(), serverZone.zone());
    }
}