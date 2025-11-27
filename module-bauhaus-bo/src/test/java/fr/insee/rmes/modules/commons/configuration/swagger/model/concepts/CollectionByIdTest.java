package fr.insee.rmes.modules.commons.configuration.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CollectionByIdTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull(){
        CollectionById collectionById = new CollectionById();
        List<Boolean> actual = List.of(collectionById.id==null,
                collectionById.prefLabelLg1==null,
                collectionById.prefLabelLg2==null,
                collectionById.descriptionLg1==null,
                collectionById.descriptionLg2==null,
                collectionById.descriptionLg2==null,
                collectionById.creator==null,
                collectionById.contributor==null,
                collectionById.isValidated==null,
                collectionById.created==null,
                collectionById.modified==null);
        List<Boolean> expected = List.of(true,true,true,true,true,true,true,true,true,true,true);
        assertEquals(expected,actual);
    }
}