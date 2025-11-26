package fr.insee.rmes.modules.commons.configuration.swagger.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class AcceptTest {

    @Test
    void shouldReturnAnyErrorWhenGetMediaType(){
         String acceptJson = Accept.JSON.getMediaType();
         String acceptXml = Accept.XML.getMediaType();
         String expectedJson = "application/json";
         String expectedXml = "application/xml";
         assertTrue(expectedJson.equals(acceptJson) && expectedXml.equals(acceptXml));
    }

    @Test
    void shouldReturnErrorWhenFromMediaType(){
        String string = "mockedFakeAccept";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Accept.fromMediaType(string));
        assertTrue(exception.getMessage().contains("Unsupported media type"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "application/json", "application/xml" })
    void shouldNotReturnErrorWhenFromMediaType(String string){
        Accept acceptActual = Accept.fromMediaType(string);
        Accept  acceptJson = Accept.JSON;
        Accept  acceptXml = Accept.XML;
        assertTrue(acceptJson==acceptActual || acceptXml==acceptActual );
    }
}