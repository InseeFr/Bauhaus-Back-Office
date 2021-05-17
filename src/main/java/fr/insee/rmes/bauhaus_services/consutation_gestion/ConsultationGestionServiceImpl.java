package fr.insee.rmes.bauhaus_services.consutation_gestion;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import jdk.vm.ci.meta.Constant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;

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

        JSONArray conceptsSdmx = repoGestion.getResponseAsArray(buildRequest("getConceptsSdmx.ftlh", params));
        if(conceptsSdmx.length() > 0){
            concept.put("conceptsSdmx", conceptsSdmx);
        }

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
    public String getStructure(String id) throws RmesException {
        String defaultDate = "2020-01-01T00:00:00.000";
        HashMap<String, Object> params = new HashMap<>();
        params.put("STRUCTURES_GRAPH", Config.STRUCTURES_GRAPH);
        params.put("STRUCTURE_ID", id);
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);

        JSONObject structure =  repoGestion.getResponseAsObject(buildRequest("getStructure.ftlh", params));

        structure.put("label", this.formatLabel(structure));
        structure.remove("prefLabelLg1");
        structure.remove("prefLabelLg2");

        if(structure.has("statutValidation")){
            String validationState = structure.getString("statutValidation");
            structure.put("statutValidation", this.getValidationState(validationState));
        }

        if(!structure.has("dateCréation")){
            structure.put("dateCréation", defaultDate);
        }
        if(!structure.has("dateMiseAJour")){
            structure.put("dateMiseAJour", defaultDate);
        }
        
        getStructureComponents(id, structure);
        return structure.toString();
    }

    private void getStructureComponents(String id, JSONObject structure) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("STRUCTURES_GRAPH", Config.STRUCTURES_GRAPH);
        params.put("STRUCTURES_COMPONENTS_GRAPH", Config.STRUCTURES_COMPONENTS_GRAPH);
        params.put("CONCEPTS_GRAPH", Config.CONCEPTS_GRAPH);
        params.put("CODELIST_GRAPH", Config.CODELIST_GRAPH);

        params.put("STRUCTURE_ID", id);
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);

        JSONArray components = repoGestion.getResponseAsArray(buildRequest("getStructureComponents.ftlh", params));

        JSONArray measures = new JSONArray();
        JSONArray dimensions = new JSONArray();
        JSONArray attributes = new JSONArray();

        for (int i = 0; i < components.length(); i++) {
            JSONObject component = components.getJSONObject(i);
            component.put("label", this.formatLabel(component));
            component.remove("prefLabelLg1");
            component.remove("prefLabelLg2");

            if(component.has("listeCodeUri")){
                component.put("representation", "liste de code");

                JSONObject listCode = new JSONObject();
                listCode.put("uri", component.getString("listeCodeUri"));
                listCode.put("id", component.getString("listeCodeNotation"));
                component.put("listCode", listCode);
                component.remove("listeCodeUri");
                component.remove("listeCodeNotation");
            }

            if(component.has("conceptUri")){

                JSONObject concept = new JSONObject();
                concept.put("uri", component.getString("conceptUri"));
                concept.put("id", component.getString("conceptId"));
                component.put("concept", concept);
                component.remove("conceptUri");
                component.remove("conceptId");
            }

            if(component.has("representation")){
                if(component.getString("representation").endsWith("date")){
                    component.put("representation", "date");
                } else if(component.getString("representation").endsWith("int")){
                    component.put("representation", "entier");
                } else if(component.getString("representation").endsWith("float")){
                    component.put("representation", "décimal");
                }
            }

            String idComponent = component.getString("id");
            component.remove("id");
            if(idComponent.startsWith("a")){
                attributes.put(component);
            }
            if(idComponent.startsWith("m")){
                measures.put(component);
            }
            if(idComponent.startsWith("d")){
                dimensions.put(component);
            }
        }
        structure.put("attributs", attributes);
        structure.put("mesures", measures);
        structure.put("dimensions", dimensions);
    }

    @Override
    public String getCodesList(String notation) throws RmesException {
        String defaultDate = "2020-01-01T00:00:00.000";

        HashMap<String, Object> params = new HashMap<>();
        params.put("CODELIST_GRAPH", Config.CODELIST_GRAPH);
        params.put("NOTATION", notation);
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);

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
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);

        JSONArray codes =  repoGestion.getResponseAsArray(buildRequest("getCodes.ftlh", params));

        JSONObject childrenMapping = new JSONObject();

        JSONObject formattedCodes = new JSONObject();

        for (int i = 0; i < codes.length(); i++) {
            JSONObject code = codes.getJSONObject(i);

            if(code.has(Constants.PARENTS)){
                JSONArray children = new JSONArray();
                String parentCode = code.getString(Constants.PARENTS);
                if(childrenMapping.has(parentCode)){
                    children = childrenMapping.getJSONArray(parentCode);
                }
                children.put(code.get("code"));
                childrenMapping.put(parentCode, children);
            }


            if(formattedCodes.has(code.getString(Constants.URI))){
                JSONObject c = formattedCodes.getJSONObject(code.getString(Constants.URI));

                if(code.has(Constants.PARENTS)){
                    JSONArray parents = c.getJSONArray(Constants.PARENTS);
                    parents.put(code.getString(Constants.PARENTS));
                    c.put(Constants.PARENTS, parents);
                }
            } else {
                code.put("label", this.formatLabel(code));
                code.remove(Constants.PREF_LABEL_LG1);
                code.remove(Constants.PREF_LABEL_LG2);

                if(code.has(Constants.PARENTS)){
                    JSONArray parents = new JSONArray();
                    parents.put(code.getString(Constants.PARENTS));
                    code.put(Constants.PARENTS, parents);
                } else {
                    code.put(Constants.PARENTS, new JSONArray());
                }
                formattedCodes.put(code.getString(Constants.URI), code);
            }
        }

        JSONArray result = new JSONArray();
        Iterator<String> keys = formattedCodes.keys();

        while(keys.hasNext()) {
            String key = keys.next();
            JSONObject code = formattedCodes.getJSONObject(key);
            if(childrenMapping.has(code.getString("code"))){
                code.put("enfants", childrenMapping.getJSONArray(code.getString("code")));
            }
            result.put(code);
        }
        return result;
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
