package fr.insee.rmes.bauhaus_services.consutation_gestion;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.persistance.ontologies.IGEO;
import fr.insee.rmes.persistance.ontologies.QB;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;

@Service
public class ConsultationGestionServiceImpl extends RdfService implements ConsultationGestionService {

    String defaultDate = "2020-01-01T00:00:00.000";

	
	@Override
    public String getDetailedConcept(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);
        params.put("ID", id);
        params.put("CONCEPTS_GRAPH", Config.CONCEPTS_GRAPH);

        JSONObject concept = repoGestion.getResponseAsObject(buildRequest("getDetailedConcept.ftlh", params));
        JSONArray labels = new JSONArray();



        String labelLg1 = concept.getString(Constants.PREF_LABEL_LG1);
        JSONObject labelLg1Object = new JSONObject();
        labelLg1Object.put("langue", Config.LG1);
        labelLg1Object.put("contenu", labelLg1);
        labels.put(labelLg1Object);
        concept.remove(Constants.PREF_LABEL_LG1);

        if(concept.has(Constants.PREF_LABEL_LG2)){
            String labelLg2 = concept.getString(Constants.PREF_LABEL_LG2);
            JSONObject labelLg2Object = new JSONObject();
            labelLg2Object.put("langue", Config.LG2);
            labelLg2Object.put("contenu", labelLg2);
            labels.put(labelLg2Object);
            concept.remove(Constants.PREF_LABEL_LG2);
        }

        concept.put(Constants.LABEL, labels);

        if(concept.has("statutValidation")){
            String validationState = concept.getString("statutValidation");
            concept.put("statutValidation", this.getValidationState(validationState));
        }

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
            String validationState = structure.getString(Constants.STATUT_VALIDATION);
            structure.put(Constants.STATUT_VALIDATION, this.getValidationState(validationState));
        }

        return structures.toString();
    }

    private String getValidationState(String validationState){
        if(ValidationStatus.VALIDATED.toString().equalsIgnoreCase(validationState) || "true".equalsIgnoreCase(validationState)){
            return "Publiée";
        }
        if(ValidationStatus.MODIFIED.toString().equalsIgnoreCase(validationState) || "false".equalsIgnoreCase(validationState)){
            return "Provisoire, déjà publiée";
        }
        if(ValidationStatus.UNPUBLISHED.toString().equalsIgnoreCase(validationState)){
            return "Provisoire, jamais publiée";
        }

        return validationState;
    }

    @Override
    public String getAllCodesLists() throws RmesException {

        HashMap<String, Object> params = new HashMap<>();
        params.put("CODELIST_GRAPH", Config.CODELIST_GRAPH);

        JSONArray codesLists =  repoGestion.getResponseAsArray(buildRequest("getAllCodesLists.ftlh", params));
        for (int i = 0; i < codesLists.length(); i++) {
            JSONObject codesList = codesLists.getJSONObject(i);
            if(!codesList.has("dateMiseAJour")){
                codesList.put("dateMiseAJour", defaultDate);
            }
            if(codesList.has(Constants.STATUT_VALIDATION)){
                String validationState = codesList.getString(Constants.STATUT_VALIDATION);
                codesList.put(Constants.STATUT_VALIDATION, this.getValidationState(validationState));
            }
        }
        return codesLists.toString();
    }

    @Override
    public String getStructure(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("STRUCTURES_GRAPH", Config.STRUCTURES_GRAPH);
        params.put("STRUCTURE_ID", id);
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);

        JSONArray structureArray =  repoGestion.getResponseAsArray(buildRequest("getStructure.ftlh", params));
        JSONObject structure = (JSONObject) structureArray.get(0);

        structure.put("label", this.formatLabel(structure));
        structure.remove("prefLabelLg1");
        structure.remove("prefLabelLg2");


        if(structureArray.length() > 1){
            JSONArray necessairePour = new JSONArray();
            for (int i = 0; i < structureArray.length(); i++) {
                necessairePour.put(structureArray.getJSONObject(i).getString("necessairePour"));

            }

            structure.put("necessairePour", necessairePour);
        }
        if(structure.has("idRelation")){
            structure.put("dsdSdmx", extractSdmx(structure.getString("idRelation")));
            structure.remove("idRelation");
        }
        if(structure.has("idParent") && structure.has("uriParent")){
            JSONObject parent = new JSONObject();
            parent.put("id", structure.getString("idParent"));
            parent.put("uri", structure.getString("uriParent"));

            if(structure.has("idParentRelation")){
                parent.put("dsdSdmx", extractSdmx(structure.getString("idParentRelation")));
                structure.remove("idParentRelation");
            }

            structure.put("parent", parent);
            structure.remove("idParent");
            structure.remove("uriParent");
        }
        if(structure.has(Constants.STATUT_VALIDATION)){
            String validationState = structure.getString(Constants.STATUT_VALIDATION);
            structure.put(Constants.STATUT_VALIDATION, this.getValidationState(validationState));
        }

        if(!structure.has("dateCreation")){
            structure.put("dateCreation", defaultDate);
        }
        if(!structure.has("dateMiseAJour")){
            structure.put("dateMiseAJour", defaultDate);
        }
        
        getStructureComponents(id, structure);
        return structure.toString();
    }

    private JSONObject extractSdmx(String originalRelation) {
        String iri = originalRelation.replace("urn:sdmx:org.sdmx.infomodel.metadatastructure.MetadataStructure=", "");
        JSONObject relation = new JSONObject();
        relation.put("id", iri.substring(iri.indexOf(":") + 1, iri.indexOf("(") ));
        relation.put("agence", iri.substring(0, iri.indexOf(":")));
        relation.put("version", iri.substring(iri.indexOf("(") + 1, iri.indexOf(")")));
        return relation;
    }

    @Override
    public String getAllComponents() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("STRUCTURES_COMPONENTS_GRAPH", Config.STRUCTURES_COMPONENTS_GRAPH);
        JSONArray components =  repoGestion.getResponseAsArray(buildRequest("getComponents.ftlh", params));

        for (int i = 0; i < components.length(); i++) {
            JSONObject component = components.getJSONObject(i);
            String validationState = component.getString("statutValidation");
            component.put("statutValidation", this.getValidationState(validationState));
        }

        return components.toString();
    }

    @Override
    public JSONObject getComponent(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("STRUCTURES_COMPONENTS_GRAPH", Config.STRUCTURES_COMPONENTS_GRAPH);
        params.put("CODELIST_GRAPH", Config.CODELIST_GRAPH);
        params.put("CONCEPTS_BASE_URI", Config.CONCEPTS_BASE_URI);
        params.put("ID", id);
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);

        JSONObject component =  repoGestion.getResponseAsObject(buildRequest("getComponent.ftlh", params));

        component.put("label", this.formatLabel(component));
        component.remove("prefLabelLg1");
        component.remove("prefLabelLg2");

        if(component.has("uriComponentParentId")){
            JSONObject parent = new JSONObject();
            parent.put("id", component.getString("uriComponentParentId"));
            parent.put("notation", component.getString("uriComponentParentNotation"));
            component.remove("uriComponentParentId");
            component.remove("uriComponentParentNotation");
            component.put("parent", parent);
        }

        JSONArray flatChildren = repoGestion.getResponseAsArray(buildRequest("getComponentChildren.ftlh", params));
        if(flatChildren.length() > 0) {
            component.put("enfants", flatChildren);
        }

        if(component.has("statutValidation")){
            String validationState = component.getString("statutValidation");
            component.put("statutValidation", this.getValidationState(validationState));
        }

        if(component.has("uriListeCode")){
            component.put("representation", "liste de code");

            JSONArray codes = getCodes(component.getString("idListeCode"));
            JSONObject listCode = new JSONObject();
            listCode.put("uri", component.getString("uriListeCode"));
            listCode.put("id", component.getString("idListeCode"));
            listCode.put("codes", codes);


            if(component.has("uriParentListCode") && component.has("idParentListCode")){
                listCode.put("ParentListeCode", new JSONObject()
                        .append("id", component.getString("idParentListCode"))
                        .append("uri", component.getString("uriParentListCode")));
                component.remove("uriParentListCode");
                component.remove("idParentListCode");
            }
            component.put("listeCode", listCode);
            component.remove("uriListeCode");
            component.remove("idListeCode");
        }

        if(component.has("uriConcept")){

            JSONObject concept = new JSONObject();
            concept.put("uri", component.getString("uriConcept"));
            concept.put("id", component.getString("idConcept"));
            this.addCloseMatch(concept);
            component.put("concept", concept);
            component.remove("uriConcept");
            component.remove("idConcept");
        }

        if(component.has("representation")){
            component.put("representation", component.getString("representation").replace(IGEO.NAMESPACE, "").replace("http://www.w3.org/2001/XMLSchema#", ""));
        }

        if(component.has("minLength") || component.has("maxLength") || component.has("minInclusive") || component.has("maxInclusive") || component.has("pattern")){
            JSONObject format = new JSONObject();

            if (component.has("minLength")) {
                format.put("longueurMin", component.get("minLength"));
                component.remove("minLength");
            }
            if (component.has("maxLength")) {
                format.put("longueurMax", component.get("maxLength"));
                component.remove("maxLength");
            }
            if (component.has("minInclusive")) {
                format.put("valeurMin", component.get("minInclusive"));
                component.remove("minInclusive");
            }
            if (component.has("maxInclusive")) {
                format.put("valeurMax", component.get("maxInclusive"));
                component.remove("maxInclusive");
            }
            if (component.has("pattern")) {
                format.put("expressionReguliere", component.get("pattern"));
                component.remove("pattern");
            }
            component.put("format", format);
        }

        if(id.startsWith("m")){
            JSONArray attributes = repoGestion.getResponseAsArray(buildRequest("getAttributeForMeasure.ftlh", params));
            if(attributes.length() > 0){
                JSONArray caracteristiques = new JSONArray();
                for(int i = 0; i < attributes.length(); i++){
                    JSONObject attribute = attributes.getJSONObject(i);
                    if(attribute.has("attributeId")){
                        JSONObject attributeLink = getComponent(attribute.getString("attributeId"));
                        JSONObject value = new JSONObject();
                        value.put("code", attribute.getString("attributeValueCode"));
                        value.put("uri", attribute.getString("attributeValueIri"));
                        attributeLink.put("value", value);
                        caracteristiques.put(attributeLink);
                    }
                }
                component.put("caracteristiques", caracteristiques);
            }
        }

        return component;
    }

    private void addCloseMatch(JSONObject concept) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("CONCEPTS_GRAPH", Config.CONCEPTS_GRAPH);
        params.put("CONCEPT_ID", concept.getString("id"));
        JSONArray closeMatch = repoGestion.getResponseAsArray(buildRequest("getCloseMatch.ftlh", params));
        if(closeMatch.length() > 0){
            JSONArray formattedCloseMatchArray  = new JSONArray();
            for(int i = 0; i < closeMatch.length(); i++){
                String iri = ((JSONObject) closeMatch.get(i)).getString("closeMatch").replace("urn:sdmx:org.sdmx.infomodel.conceptscheme.Concept=", "");;
                JSONObject relation = new JSONObject();
                relation.put("agence", iri.substring(0, iri.indexOf(":")));
                relation.put("id", iri.substring(iri.lastIndexOf(".") + 1));
                formattedCloseMatchArray.put(relation);
            }
            concept.put("conceptsSdmx", formattedCloseMatchArray);
        }

    }

    private void getStructureComponents(String id, JSONObject structure) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("STRUCTURES_GRAPH", Config.STRUCTURES_GRAPH);
        params.put("STRUCTURES_COMPONENTS_GRAPH", Config.STRUCTURES_COMPONENTS_GRAPH);
        params.put("STRUCTURE_ID", id);

        JSONArray components = repoGestion.getResponseAsArray(buildRequest("getStructureComponents.ftlh", params));

        JSONArray measures = new JSONArray();
        JSONArray dimensions = new JSONArray();
        JSONArray attributes = new JSONArray();

        for (int i = 0; i < components.length(); i++) {
            JSONObject componentSpecification = components.getJSONObject(i);
            String idComponent = componentSpecification.getString("id");
            JSONObject component = getComponent(idComponent);

            if(componentSpecification.has("ordre")){
                component.put("ordre", componentSpecification.getString("ordre"));
            }
            if(componentSpecification.has("attachement")){
                component.put("attachement", componentSpecification.getString("attachement").replace(QB.NAMESPACE, ""));
            }
            if(componentSpecification.has("obligatoire")){
                component.put("obligatoire", componentSpecification.getString("obligatoire").equalsIgnoreCase("true") ? "oui": "non");
            }

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
        HashMap<String, Object> params = new HashMap<>();
        params.put("CODELIST_GRAPH", Config.CODELIST_GRAPH);
        params.put("NOTATION", notation);
        params.put("LG1", Config.LG1);
        params.put("LG2", Config.LG2);

        JSONObject codesList =  repoGestion.getResponseAsObject(buildRequest("getCodesList.ftlh", params));

        codesList.put("label", this.formatLabel(codesList));
        codesList.remove("prefLabelLg1");
        codesList.remove("prefLabelLg2");

        if(codesList.has(Constants.STATUT_VALIDATION)){
            String validationState = codesList.getString(Constants.STATUT_VALIDATION);
            codesList.put(Constants.STATUT_VALIDATION, this.getValidationState(validationState));
        }

        if(!codesList.has("dateCreation")){
            codesList.put("dateCreation", defaultDate);
        }
        if(!codesList.has("dateMiseAJour")){
            codesList.put("dateMiseAJour", defaultDate);
        }

        JSONArray levels = repoGestion.getResponseAsArray(buildRequest("getCodesListLevel.ftlh", params));
        if(levels.length() > 0){
            JSONArray formattedLevels = new JSONArray();
            for(int i = 0; i < levels.length(); i++){
                JSONObject level = new JSONObject();
                level.put("id", levels.getJSONObject(i).getString("idNiveau"));
                level.put("label", this.formatLabel(levels.getJSONObject(i)));
                if (levels.getJSONObject(i).has("idNiveauSuivant")) {
                    level.put("niveauxSuivants", new JSONArray().put(new JSONObject().put("id", levels.getJSONObject(i).get("idNiveauSuivant"))));
                }
                formattedLevels.put(level);
            }
            codesList.put("niveaux", formattedLevels);
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
        JSONArray levels =  repoGestion.getResponseAsArray(buildRequest("getCodeLevel.ftlh", params));

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

            JSONArray codeLevels = new JSONArray();
            for(int j = 0; j < levels.length(); j++){
                if(levels.getJSONObject(j).getString(Constants.URI).equalsIgnoreCase(code.getString(Constants.URI))){
                    codeLevels.put(new JSONObject().put("id", levels.getJSONObject(j).getString("idNiveau")));
                }
            }
            if(codeLevels.length() > 0){
                code.put("niveaux", codeLevels);
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
            if(code.getJSONArray(Constants.PARENTS).length() == 0){
                code.remove(Constants.PARENTS);
            }
            result.put(code);
        }
        return result;
    }

    private JSONArray formatLabel(JSONObject obj) {
        JSONArray label = new JSONArray();


        if(obj.has("prefLabelLg1")){
            JSONObject lg1 = new JSONObject();
            lg1.put("langue", Config.LG1);
            lg1.put("contenu", obj.getString("prefLabelLg1"));
            label.put(lg1);

        }
        if(obj.has("prefLabelLg2")){
            JSONObject lg2 = new JSONObject();
            lg2.put("langue", Config.LG2);
            lg2.put("contenu", obj.getString("prefLabelLg2"));
            label.put(lg2);
        }


        return label;
    }

    private static String buildRequest(String fileName, HashMap<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("consultation-gestion/", fileName, params);
    }
}
