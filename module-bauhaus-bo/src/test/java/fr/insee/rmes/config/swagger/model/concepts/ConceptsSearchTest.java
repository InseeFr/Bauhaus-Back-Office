package fr.insee.rmes.config.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConceptsSearchTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull(){
        ConceptsSearch conceptsSearch= new ConceptsSearch();
        List<Boolean> actual = List.of(conceptsSearch.id==null,
                conceptsSearch.label==null,
                conceptsSearch.creator==null,
                conceptsSearch.disseminationStatus==null,
                conceptsSearch.validationStatus==null,
                conceptsSearch.definition==null,
                conceptsSearch.created==null,
                conceptsSearch.valid==null,
                conceptsSearch.modified==null);
        List<Boolean> expected = List.of(true,true,true,true,true,true,true,true,true);
        assertEquals(expected,actual);
    }

}