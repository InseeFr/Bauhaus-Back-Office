package fr.insee.rmes.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class UriUtilsTest {

    @Test
    void getLastPartFromUri() {
        String exampleOne = UriUtils.getLastPartFromUri("BauhausBack");
        String exampleTwo = UriUtils.getLastPartFromUri("Bauhaus\\Back\\");
        String exampleTree = UriUtils.getLastPartFromUri("Bauhaus\\Back");
        assertEquals("[, , Back]", List.of(exampleOne, exampleTwo, exampleTree).toString());
    }
}
