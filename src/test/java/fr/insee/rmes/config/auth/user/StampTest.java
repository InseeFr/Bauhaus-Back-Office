package fr.insee.rmes.config.auth.user;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StampTest {

    @Test
    void shouldGetterStampValueCorrespondsToTheExpectedValue() {
        Stamp stamp = new Stamp("mockedStamp");
        assertEquals("mockedStamp", stamp.stamp());
    }

}