package fr.insee.rmes.bauhaus_services.structures.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;

@Service
public class StructureComponentImpl extends RdfService implements StructureComponent {
    static final Logger logger = LogManager.getLogger(StructureComponentImpl.class);

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
        JSONObject response = repoGestion.getResponseAsObject(StructureQueries.getComponent(id));
        if(response.keySet().isEmpty()){
            throw new RmesNotFoundException("This component does not exist", id);
        }
        return structureComponentUtils.formatComponent(id, response);
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
            throw new NotFoundException("This component does not exist");
        }
        String type = response.getString("type");
        structureComponentUtils.deleteComponent(response, id, type);
    }

    @Override
    public String publishComponent(String id) throws RmesException {
        return structureComponentUtils.publishComponent(this.getComponentObject(id));
    }
}
