package fr.insee.rmes.modules.commons.configuration.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConceptLinksTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull(){
        ConceptLinks conceptLinks= new ConceptLinks();
        List<Boolean> actual = List.of(conceptLinks.id==null,
                conceptLinks.prefLabelLg1==null,
                conceptLinks.prefLabelLg2==null,
                conceptLinks.typeOfLink==null);
        List<Boolean> expected = List.of(true,true,true,true);
        assertEquals(expected,actual);
    }

}