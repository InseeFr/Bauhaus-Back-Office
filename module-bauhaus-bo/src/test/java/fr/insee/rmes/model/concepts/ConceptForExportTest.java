package fr.insee.rmes.model.concepts;

import fr.insee.rmes.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConceptForExportTest {

    @Test
    void shouldAddNotesWhenJsonObjectAdded() {

        ConceptForExport conceptForExport = new ConceptForExport();
        String scopeNoteLg1Before = conceptForExport.getScopeNoteLg1();
        String scopeNoteLg2Before = conceptForExport.getScopeNoteLg2();
        String definitionLg1Before = conceptForExport.getDefinitionLg1();
        String definitionLg2Before = conceptForExport.getDefinitionLg2();
        String editorialNoteLg1Before = conceptForExport.getEditorialNoteLg1();
        String editorialNoteLg2Before = conceptForExport.getEditorialNoteLg2();

        JSONObject notes = new JSONObject();
        notes.put("scopeNoteLg1", "scopeNoteLg1Example");
        notes.put("scopeNoteLg2", "scopeNoteLg2Example");
        notes.put("definitionLg1", "definitionLg1Example");
        notes.put("definitionLg2", "definitionLg2Example");
        notes.put("editorialNoteLg1", "editorialNoteLg1Example");
        notes.put("editorialNoteLg2", "editorialNoteLg2Example");

        conceptForExport.addNotes(notes);

        Boolean scopeNoteLg1After = conceptForExport.getScopeNoteLg1().equals(scopeNoteLg1Before);
        Boolean scopeNoteLg2After = conceptForExport.getScopeNoteLg2().equals(scopeNoteLg2Before);
        Boolean definitionLg1After = conceptForExport.getDefinitionLg1().equals(definitionLg1Before);
        Boolean definitionLg2After = conceptForExport.getDefinitionLg2().equals(definitionLg2Before);
        Boolean editorialNoteLg1After = conceptForExport.getEditorialNoteLg1().equals(editorialNoteLg1Before);
        Boolean editorialNoteLg2After = conceptForExport.getEditorialNoteLg2().equals(editorialNoteLg2Before);

        List<Boolean> conceptForExportAfter = List.of(scopeNoteLg1After,scopeNoteLg2After,definitionLg1After,definitionLg2After,editorialNoteLg1After,editorialNoteLg2After);

        assertEquals(List.of(false,false,false,false,false,false),conceptForExportAfter);
    }

    @Test
    void shouldAddLinksWhenJsonArrayAdded() {

        ConceptForExport conceptForExport = new ConceptForExport();

        JSONObject jsonA= new JSONObject().put("typeOfLink","narrower").put(Constants.PREF_LABEL_LG1,"A1").put(Constants.PREF_LABEL_LG2,"A2");
        JSONObject jsonB= new JSONObject().put("typeOfLink","broader").put(Constants.PREF_LABEL_LG1,"B1").put(Constants.PREF_LABEL_LG2,"B2");
        JSONObject jsonC= new JSONObject().put("typeOfLink","references").put(Constants.PREF_LABEL_LG1,"C1").put(Constants.PREF_LABEL_LG2,"C2");
        JSONObject jsonD= new JSONObject().put("typeOfLink","succeed").put(Constants.PREF_LABEL_LG1,"D1").put(Constants.PREF_LABEL_LG2,"D2");
        JSONObject jsonE= new JSONObject().put("typeOfLink","related").put(Constants.PREF_LABEL_LG1,"E1").put(Constants.PREF_LABEL_LG2,"E2");
        JSONArray links = new JSONArray().put(jsonA).put(jsonB).put(jsonC).put(jsonD).put(jsonE);

        String narrowerLg1Before = conceptForExport.getNarrowerLg1().toString();
        String narrowerLg2Before  = conceptForExport.getNarrowerLg2().toString();
        String broaderLg1Before  = conceptForExport.getBroaderLg1().toString();
        String broaderLg2Before  = conceptForExport.getBroaderLg2().toString();
        String referencesLg1Before  = conceptForExport.getReferencesLg1().toString();
        String referencesLg2Before  = conceptForExport.getReferencesLg2().toString();
        String succeedLg1Before  = conceptForExport.getSucceedLg1().toString();
        String succeedLg2Before  = conceptForExport.getSucceedLg2().toString();
        String relatedLg1Before  = conceptForExport.getRelatedLg1().toString();
        String relatedLg2Before  = conceptForExport.getRelatedLg2().toString();

        conceptForExport.addLinks(links);

        Boolean narrowerLg1After = conceptForExport.getNarrowerLg1().toString().equals(narrowerLg1Before);
        Boolean narrowerLg2After = conceptForExport.getNarrowerLg2().toString().equals(narrowerLg2Before);
        Boolean broaderLg1After = conceptForExport.getBroaderLg1().toString().equals(broaderLg1Before);
        Boolean broaderLg2After = conceptForExport.getBroaderLg2().toString().equals(broaderLg2Before);
        Boolean referencesLg1After = conceptForExport.getReferencesLg1().toString().equals(referencesLg1Before);
        Boolean referencesLg2After = conceptForExport.getReferencesLg2().toString().equals(referencesLg2Before);
        Boolean succeedLg1After = conceptForExport.getSucceedLg1().toString().equals(succeedLg1Before);
        Boolean succeedLg2After = conceptForExport.getSucceedLg2().toString().equals(succeedLg2Before);
        Boolean relatedLg1After = conceptForExport.getRelatedLg1().toString().equals(relatedLg1Before);
        Boolean relatedLg2After = conceptForExport.getRelatedLg2().toString().equals(relatedLg2Before);

        List<Boolean> conceptForExportAfter = List.of(narrowerLg1After,narrowerLg2After,broaderLg1After,broaderLg2After,referencesLg1After,referencesLg2After,succeedLg1After,succeedLg2After,relatedLg1After,relatedLg2After);

        assertEquals(List.of(false,false,false,false,false,false,false,false,false,false),conceptForExportAfter);

    }

}