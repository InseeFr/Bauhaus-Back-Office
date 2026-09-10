package fr.insee.rmes.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class PropertiesUtilsTest {

    @Test
    void shouldFindByName() {

        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("myKey", "myProperties");

        PropertiesUtils propertiesUtils = new PropertiesUtils(mockEnvironment);

        String response = propertiesUtils.findByName("myKey").toString();
        assertEquals("Optional[myProperties]", response);
    }
}
