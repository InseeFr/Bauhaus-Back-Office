package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UriUtilsTest {

    @Test
    void getLastPartFromUri() {
        String exampleOne = UriUtils.getLastPartFromUri("BauhausBack");
        String exampleTwo = UriUtils.getLastPartFromUri("Bauhaus\\Back\\");
        String exampleTree = UriUtils.getLastPartFromUri("Bauhaus\\Back");
        assertEquals("[, , Back]", List.of(exampleOne,exampleTwo,exampleTree).toString());
    }
}