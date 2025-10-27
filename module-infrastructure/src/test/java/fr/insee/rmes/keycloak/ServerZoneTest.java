package fr.insee.rmes.keycloak;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerZoneTest {

    @Test
    void shouldCreateServerZoneWithValidZone() {
        ServerZone serverZone = new ServerZone("INTERNE");
        assertEquals(ServerZone.Zone.INTERNE, serverZone.zone());
    }

    @Test
    void shouldCreateServerZoneWithDMZZone() {
        ServerZone serverZone = new ServerZone("DMZ");
        assertEquals(ServerZone.Zone.DMZ, serverZone.zone());
    }

    @Test
    void shouldCreateServerZoneWithLowercaseZone() {
        ServerZone serverZone = new ServerZone("interne");
        assertEquals(ServerZone.Zone.INTERNE, serverZone.zone());
    }

    @Test
    void shouldCreateServerZoneWithMixedCaseZone() {
        ServerZone serverZone = new ServerZone("InTeRnE");
        assertEquals(ServerZone.Zone.INTERNE, serverZone.zone());
    }

    @Test
    void shouldUseDefaultZoneForInvalidZone() {
        ServerZone serverZone = new ServerZone("INVALID");
        assertEquals(ServerZone.Zone.defaultZone(), serverZone.zone());
    }



    @Test
    void shouldUseDefaultZoneForEmptyZone() {
        ServerZone serverZone = new ServerZone("");
        assertEquals(ServerZone.Zone.defaultZone(), serverZone.zone());
    }

    @Test
    void shouldSetZoneCorrectly() {
        ServerZone serverZone = new ServerZone();
        serverZone.setZone("DMZ");
        assertEquals(ServerZone.Zone.DMZ, serverZone.zone());
    }

    @Test
    void shouldSetZoneWithLowercase() {
        ServerZone serverZone = new ServerZone();
        serverZone.setZone("dmz");
        assertEquals(ServerZone.Zone.DMZ, serverZone.zone());
    }

    @Test
    void shouldCreateDefaultZone() {
        ServerZone defaultZone = ServerZone.defaultZone();
        assertEquals(ServerZone.Zone.defaultZone(), defaultZone.zone());
    }

    @Test
    void shouldHaveCorrectToStringImplementation() {
        ServerZone serverZone = new ServerZone("INTERNE");
        String toString = serverZone.toString();
        
        assertTrue(toString.contains("ServerZone"));
        assertTrue(toString.contains("INTERNE"));
    }

    @Test
    void shouldHaveCorrectDefaultZoneInEnum() {
        assertEquals(ServerZone.Zone.INTERNE, ServerZone.Zone.defaultZone());
    }

    @Test
    void shouldHaveAllExpectedZoneValues() {
        ServerZone.Zone[] zones = ServerZone.Zone.values();
        assertEquals(2, zones.length);
        
        boolean hasInterne = false;
        boolean hasDMZ = false;
        
        for (ServerZone.Zone zone : zones) {
            if (zone == ServerZone.Zone.INTERNE) hasInterne = true;
            if (zone == ServerZone.Zone.DMZ) hasDMZ = true;
        }
        
        assertTrue(hasInterne);
        assertTrue(hasDMZ);
    }

    @Test
    void shouldHandleWhitespaceInZoneName() {
        ServerZone serverZone = new ServerZone(" INTERNE ");
        // Should use default zone since " INTERNE " != "INTERNE"
        assertEquals(ServerZone.Zone.defaultZone(), serverZone.zone());
    }

    @Test
    void shouldBeConsistentBetweenConstructorAndSetter() {
        ServerZone serverZone1 = new ServerZone("DMZ");
        
        ServerZone serverZone2 = new ServerZone();
        serverZone2.setZone("DMZ");
        
        assertEquals(serverZone1.zone(), serverZone2.zone());
    }

    @Test
    void shouldCreateEmptyServerZone() {
        ServerZone serverZone = new ServerZone();
        // Zone should be null initially
        assertNull(serverZone.zone());
    }
}