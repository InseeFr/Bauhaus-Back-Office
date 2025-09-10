package fr.insee.rmes.config.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConceptNotesTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull(){
        ConceptNotes conceptNotes= new ConceptNotes();
        List<Boolean> actual = List.of(conceptNotes.definitionLg1==null,
                conceptNotes.definitionLg2==null,
                conceptNotes.scopeNoteLg1==null,
                conceptNotes.scopeNoteLg2==null,
                conceptNotes.editorialNoteLg1==null,
                conceptNotes.editorialNoteLg2==null,
                conceptNotes.changeNoteLg1==null,
                conceptNotes.changeNoteLg2==null);
        List<Boolean> expected = List.of(true,true,true,true,true,true,true,true);
        assertEquals(expected,actual);
    }
}