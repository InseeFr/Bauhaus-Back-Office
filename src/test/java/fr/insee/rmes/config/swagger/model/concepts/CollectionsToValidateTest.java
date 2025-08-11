package fr.insee.rmes.config.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CollectionsToValidateTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull(){
        CollectionsToValidate collectionsToValidate= new CollectionsToValidate();
        List<Boolean> actual = List.of(collectionsToValidate.id==null,
                collectionsToValidate.label==null,
                collectionsToValidate.creator==null);
        List<Boolean> expected = List.of(true,true,true);
        assertEquals(expected,actual);
    }
}