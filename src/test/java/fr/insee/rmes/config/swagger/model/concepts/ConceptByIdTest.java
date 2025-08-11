package fr.insee.rmes.config.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConceptByIdTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull(){
        ConceptById conceptById= new ConceptById();
        List<Boolean> actual = List.of(conceptById.id==null,
                conceptById.prefLabelLg1==null,
                conceptById.prefLabelLg2==null,
                conceptById.altLabelLg1==null,
                conceptById.altLabelLg2==null,
                conceptById.creator==null,
                conceptById.contributor==null,
                conceptById.disseminationStatus==null,
                conceptById.isValidated==null,
                conceptById.additionalMaterial==null,
                conceptById.conceptVersion==null,
                conceptById.additionalMaterial==null,
                conceptById.created==null,
                conceptById.modified==null,
                conceptById.valid==null);
        List<Boolean> expected = List.of(true,true,true,true,true,true,true,true,true,true,true,true,true,true,true);
        assertEquals(expected,actual);
    }

}

