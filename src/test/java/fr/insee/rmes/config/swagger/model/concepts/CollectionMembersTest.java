package fr.insee.rmes.config.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CollectionMembersTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull(){
        CollectionMembers collectionMembers = new CollectionMembers();
        List<Boolean> actual = List.of(collectionMembers.id==null,
                collectionMembers.prefLabelLg1==null,
                collectionMembers.prefLabelLg2==null);
        List<Boolean> expected = List.of(true,true,true);
        assertEquals(expected,actual);
    }
}