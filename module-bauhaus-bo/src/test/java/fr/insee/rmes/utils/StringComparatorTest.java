package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringComparatorTest {

    @Test
    void shouldCompareTwoStrings() {

        StringComparator stringComparator = new StringComparator();
        int exampleOne = stringComparator.compare("Bauhaus","Bauhaus");
        int exampleTwo = stringComparator.compare("Bauhaus", "Bauhaus-back-office");
        int exampleThree = stringComparator.compare("Bauhaus-back-office","Bauhaus");

        List<Integer> response = List.of(exampleOne,exampleTwo,exampleThree);

        assertEquals(response, List.of(0,-1,1));
    }
}