package fr.insee.rmes.bauhaus_services.concepts.concepts;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.notes.NoteManager;
import fr.insee.rmes.model.concepts.ConceptForExport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConceptsUtilsTest {

    @Test
    void shouldReturnGetConceptExportFileName() {

        ConceptsPublication conceptsPublication =  new ConceptsPublication();
        NoteManager noteManager = new NoteManager();
        ConceptsUtils conceptsUtils = new ConceptsUtils(conceptsPublication,noteManager,19);

        ConceptForExport conceptForExport = new ConceptForExport();
        conceptForExport.setId("id");
        conceptForExport.setPrefLabelLg1("prefLabel1");
        conceptForExport.setPrefLabelLg2("prefLabel2");

        String response = conceptsUtils.getConceptExportFileName(conceptForExport);

        assertEquals("idPreflabel1",response);

    }
}