package fr.insee.rmes.bauhaus_services.structures.impl;

import javax.ws.rs.NotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils;
import fr.insee.rmes.exceptions.RmesException;
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
        logger.info("Starting to get all mutualized components");
        return repoGestion.getResponseAsArray(StructureQueries.getComponentsForSearch()).toString();
    }

    @Override
    public String getComponents() throws RmesException {

        logger.info("Starting to get all mutualized components");
        return repoGestion.getResponseAsArray(StructureQueries.getComponents()).toString();
    }

    @Override
    public String getComponent(String id) throws RmesException {

        logger.info("Starting to get one mutualized component");
        JSONObject response = repoGestion.getResponseAsObject(StructureQueries.getComponent(id));
        if(response.keySet().isEmpty()){
            throw new NotFoundException("This component do not exist");
        }
        return structureComponentUtils.formatComponent(id, response);
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
        JSONObject response = repoGestion.getResponseAsObject(StructureQueries.getComponent(id));
        if(response.keySet().isEmpty()){
            throw new NotFoundException("This component do not exist");
        }
        structureComponentUtils.deleteComponent(response, id);
    }
}
