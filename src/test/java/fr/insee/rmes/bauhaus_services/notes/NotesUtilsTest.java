package fr.insee.rmes.bauhaus_services.notes;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.Concept;
import fr.insee.rmes.model.notes.VersionableNote;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotesUtilsTest {

    @Test
    void shouldGetVersion() throws RmesException {
        Concept concept = new Concept("id",true);
        VersionableNote versionableNote = new VersionableNote();
        String defaultBVersion = "defaultVersion";
        NotesUtils notesUtils = new NotesUtils();
        String result= notesUtils.getVersion(concept,versionableNote,defaultBVersion);
        assertEquals("1",result);
    }
}