package fr.insee.rmes.bauhaus_services.consutation_gestion;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
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
            String validationState = structure.getString("validationState");
            if("Validated".equalsIgnoreCase(validationState)){
                structure.put("validationState", "Publiée");
            }
            if("Modified".equalsIgnoreCase(validationState)){
                structure.put("validationState", "Provisoire, déjà publiée");
            }
            if("Unpublished".equalsIgnoreCase(validationState)){
                structure.put("validationState", "Provisoire, jamais publiée");
            }
        }

        return structures.toString();
    }

    private static String buildRequest(String fileName, HashMap<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("consultation-gestion/", fileName, params);
    }
}
