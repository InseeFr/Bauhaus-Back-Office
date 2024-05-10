package fr.insee.rmes.bauhaus_services.structures.impl;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StructureComponentImpl extends RdfService implements StructureComponent {
    static final Logger logger = LoggerFactory.getLogger(StructureComponentImpl.class);
    public static final String ATTRIBUTE_IRI = "attributeIRI";
    public static final String VALUE_IRI = "valueIri";

    @Autowired
    StructureComponentUtils structureComponentUtils;

    /**
     * Return all mutualized components
     * @return
     */
    @Override
    public String getComponentsForSearch() throws RmesException {
        logger.info("Getting all mutualized components");
        return repoGestion.getResponseAsArray(StructureQueries.getComponents(true, true, true)).toString();
    }

    @Override
    public String getAttributes() throws RmesException {
        logger.info("Getting all mutualized attributes");
        return repoGestion.getResponseAsArray(StructureQueries.getComponents(true, false, false)).toString();
    }

    @Override
    public String getComponents() throws RmesException {

        logger.info("Getting all mutualized components");
        return repoGestion.getResponseAsArray(StructureQueries.getComponents(true, true, true)).toString();
    }

    public JSONObject getComponentObject(String id) throws RmesException {

        logger.info("Starting to get one mutualized component");
        JSONArray response = repoGestion.getResponseAsArray(StructureQueries.getComponent(id));


        if(response.length() == 0){
            throw new RmesNotFoundException("This component does not exist", id);
        }

        // We first format linked attributes if they exists
        JSONObject component = new JSONObject(response.getJSONObject(0).toMap());

        getMultipleTripletsForObject(component, "contributor", StructureQueries.getComponentContributors(component.getString("component")), "contributor");
        component.remove("component");

        if(component.has(ATTRIBUTE_IRI)){
            component.remove(ATTRIBUTE_IRI);
        }
        if(component.has(VALUE_IRI)){
            component.remove(VALUE_IRI);
        }

        int index = 0;
        for (int i = 0; i < response.length(); i++) {
            JSONObject current = response.getJSONObject(i);
            if(current.has(ATTRIBUTE_IRI) && current.has(VALUE_IRI) && !current.getString(ATTRIBUTE_IRI).isEmpty() && !current.getString(VALUE_IRI).isEmpty()){
                component.put("attribute_" + index, current.getString(ATTRIBUTE_IRI));
                component.put("attributeValue_" + index, current.getString(VALUE_IRI));
                index++;
            }
        }

        return structureComponentUtils.formatComponent(id, component);
    }

    @Override
    public String getComponent(String id) throws RmesException {
        return this.getComponentObject(id).toString();
    }

    @Override
    public String updateComponent(String componentId, String body) throws RmesException {
        return structureComponentUtils.updateComponent(componentId, body);
    }

    @Override
    public String createComponent( String body) throws RmesException {
        return structureComponentUtils.createComponent(body);
    }

    @Override
    public void deleteComponent(String id) throws RmesException {
        JSONObject response = this.getComponentObject(id);
        if(response.keySet().isEmpty()){
            throw new RmesNotFoundException("Not Found","component with "+id+" not found");
        }
        String type = response.getString("type");
        structureComponentUtils.deleteComponent(response, id, type);
    }

    @Override
    public String publishComponent(String id) throws RmesException {
        return structureComponentUtils.publishComponent(this.getComponentObject(id));
    }
}
