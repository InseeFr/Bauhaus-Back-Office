package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionsUtilsTest {

    @Test
    void shouldSetCollection() {
        CollectionsUtils collectionsUtils = new CollectionsUtils();
        RmesException exception = assertThrows(RmesException.class, () -> collectionsUtils.setCollection("dfrfefe"));
        Assertions.assertTrue(exception.getDetails().contains("IOException"));
    }
}