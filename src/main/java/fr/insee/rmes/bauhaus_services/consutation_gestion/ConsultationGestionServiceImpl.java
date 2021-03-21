package fr.insee.rmes.bauhaus_services.consutation_gestion;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ConsultationGestionServiceImpl extends RdfService implements ConsultationGestionService {
    @Override
    public String getDetailedConcept(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);
        params.put("ID", id);
        params.put("CONCEPTS_GRAPH", Config.CONCEPTS_GRAPH);

        JSONObject concept = repoGestion.getResponseAsObject(buildRequest("getDetailedConcept.ftlh", params));
        JSONArray labels = new JSONArray();


        String labelLg1 = concept.getString("prefLabelLg1");
        JSONObject labelLg1Object = new JSONObject();
        labelLg1Object.put("langue", Config.LG1);
        labelLg1Object.put("contenu", labelLg1);
        labels.put(labelLg1Object);
        concept.remove("prefLabelLg1");

        if(concept.has("prefLabelLg2")){
            String labelLg2 = concept.getString("prefLabelLg2");
            JSONObject labelLg2Object = new JSONObject();
            labelLg2Object.put("langue", Config.LG2);
            labelLg2Object.put("contenu", labelLg2);
            labels.put(labelLg2Object);
            concept.remove("prefLabelLg2");
        }

        concept.put("label", labels);

        return concept.toString();
    }

    @Override
    public String getAllConcepts() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);
        params.put("CONCEPTS_GRAPH", Config.CONCEPTS_GRAPH);
        return repoGestion.getResponseAsArray(buildRequest("getAllConcepts.ftlh", params)).toString();
    }

    @Override
    public String getAllStructures() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("STRUCTURES_GRAPH", Config.STRUCTURES_GRAPH);
        JSONArray structures =  repoGestion.getResponseAsArray(buildRequest("getStructures.ftlh", params));

        for (int i = 0; i < structures.length(); i++) {
            JSONObject structure = structures.getJSONObject(i);
            String validationState = structure.getString("statutValidation");
            structure.put("statutValidation", this.getValidationState(validationState));
        }

        return structures.toString();
    }

    private String getValidationState(String validationState){
        if(ValidationStatus.VALIDATED.toString().equalsIgnoreCase(validationState)){
            return "Publiée";
        }
        if(ValidationStatus.MODIFIED.toString().equalsIgnoreCase(validationState)){
            return "Provisoire, déjà publiée";
        }
        if(ValidationStatus.UNPUBLISHED.toString().equalsIgnoreCase(validationState)){
            return "Provisoire, jamais publiée";
        }

        return validationState;
    }

    @Override
    public String getAllCodesLists() throws RmesException {
        String defaultDate = "2020-01-01T00:00:00.000";

        HashMap<String, Object> params = new HashMap<>();
        params.put("CODELIST_GRAPH", Config.CODELIST_GRAPH);

        JSONArray codesLists =  repoGestion.getResponseAsArray(buildRequest("getAllCodesLists.ftlh", params));
        for (int i = 0; i < codesLists.length(); i++) {
            JSONObject codesList = codesLists.getJSONObject(i);
            if(!codesList.has("dateMiseAJour")){
                codesList.put("dateMiseAJour", defaultDate);
            }
            if(codesList.has("statutValidation")){
                String validationState = codesList.getString("statutValidation");
                codesList.put("statutValidation", this.getValidationState(validationState));
            }
        }
        return codesLists.toString();
    }

    @Override
    public String getCodesList(String notation) throws RmesException {
        String defaultDate = "2020-01-01T00:00:00.000";

        HashMap<String, Object> params = new HashMap<>();
        params.put("CODELIST_GRAPH", Config.CODELIST_GRAPH);
        params.put("NOTATION", notation);

        JSONObject codesList =  repoGestion.getResponseAsObject(buildRequest("getCodesList.ftlh", params));

        codesList.put("label", this.formatLabel(codesList));
        codesList.remove("prefLabelLg1");
        codesList.remove("prefLabelLg2");

        if(codesList.has("statutValidation")){
            String validationState = codesList.getString("statutValidation");
            codesList.put("statutValidation", this.getValidationState(validationState));
        }

        if(!codesList.has("dateCréation")){
            codesList.put("dateCréation", defaultDate);
        }
        if(!codesList.has("dateMiseAJour")){
            codesList.put("dateMiseAJour", defaultDate);
        }

        codesList.put("codes", this.getCodes(notation));

        return codesList.toString();
    }

    private JSONArray getCodes(String notation) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("CODELIST_GRAPH", Config.CODELIST_GRAPH);
        params.put("NOTATION", notation);

        JSONArray codes =  repoGestion.getResponseAsArray(buildRequest("getCodes.ftlh", params));

        for (int i = 0; i < codes.length(); i++) {
            JSONObject code = codes.getJSONObject(i);
            code.put("label", this.formatLabel(code));
            code.remove("prefLabelLg1");
            code.remove("prefLabelLg2");
        }

        return codes;
    }

    private JSONArray formatLabel(JSONObject obj) {
        JSONArray label = new JSONArray();

        JSONObject lg1 = new JSONObject();
        JSONObject lg2 = new JSONObject();

        lg1.put("langue", Config.LG1);
        lg2.put("langue", Config.LG2);
        lg1.put("contenu", obj.getString("prefLabelLg1"));
        lg2.put("contenu", obj.getString("prefLabelLg2"));

        label.put(lg1);
        label.put(lg2);

        return label;
    }

    private static String buildRequest(String fileName, HashMap<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("consultation-gestion/", fileName, params);
    }
}
