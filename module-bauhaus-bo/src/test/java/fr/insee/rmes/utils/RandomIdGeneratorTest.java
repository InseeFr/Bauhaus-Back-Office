package fr.insee.rmes.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RandomIdGeneratorTest {

    @Test
    void shouldGenerateCollectionIdIdOfVersion4() {
        IdGenerator idGenerator = new IdGenerator();
        String uuid = idGenerator.generateNextId();
        // la version d'un UUID est donnée par le 13eme caractere de l'uuid (sans compter les "-")
        String cleanUUID = uuid.replace("-", "");
        Character version = cleanUUID.charAt(12);
        assertEquals('4', version);
    }
}
