package fr.insee.rmes.modules.commons.configuration.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CollectionsDashboardTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull(){
        CollectionsDashboard collectionsDashboard= new CollectionsDashboard();
        List<Boolean> actual = List.of(collectionsDashboard.id==null,
                collectionsDashboard.label==null,
                collectionsDashboard.creator==null,
                collectionsDashboard.isValidated==null,
                collectionsDashboard.created==null,
                collectionsDashboard.modified==null,
                collectionsDashboard.modified==null,
                collectionsDashboard.nbMembers==null);
        List<Boolean> expected = List.of(true,true,true,true,true,true,true,true);
        assertEquals(expected,actual);
    }

}