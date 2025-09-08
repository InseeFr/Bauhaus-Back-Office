package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionsUtilsTest {

    @Test
    void shouldThrowAnExceptionWhenSetCollectionOnlyWithId() {
        CollectionsUtils collectionsUtils = new CollectionsUtils();
        RmesException exception = assertThrows(RmesException.class, () -> collectionsUtils.setCollection("mocked id"));
        Assertions.assertTrue(exception.getDetails().contains("IOException"));
    }

    @Test
    void shouldThrowAnExceptionWhenSetCollectionWithIdAndBody() {
        CollectionsUtils collectionsUtils = new CollectionsUtils();
        RmesException exception = assertThrows(RmesException.class, () -> collectionsUtils.setCollection("mocked id","mocked body"));
        Assertions.assertTrue(exception.getDetails().contains("IOException"));
    }

}