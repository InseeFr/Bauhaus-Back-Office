package fr.insee.rmes.bauhaus_services.structures.impl;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.persistence.StructureComponentRepository;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructureComponent;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.IdGenerator;
import java.util.List;
import java.util.stream.IntStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StructureComponentImpl extends RdfService implements StructureComponent {
    static final Logger logger = LoggerFactory.getLogger(StructureComponentImpl.class);
    public static final String ATTRIBUTE_IRI = "attributeIRI";
    public static final String VALUE_IRI = "valueIri";

    private final StructureComponentRepository structureComponentRepository;

    private final StructureQueries structureQueries;

    public StructureComponentImpl(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils,
            StructureComponentRepository structureComponentRepository,
            StructureQueries structureQueries) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.structureComponentRepository = structureComponentRepository;
        this.structureQueries = structureQueries;
    }

    /**
     * Return all mutualized components
     * @return
     */
    @Override
    public String getComponentsForSearch() throws RmesException {
        logger.info("Getting all mutualized components");
        return repoGestion
                .getResponseAsArray(structureQueries.getComponents(true, true, true))
                .toString();
    }

    @Override
    public String getAttributes() throws RmesException {
        logger.info("Getting all mutualized attributes");
        return repoGestion
                .getResponseAsArray(structureQueries.getComponents(true, false, false))
                .toString();
    }

    @Override
    public List<PartialStructureComponent> getComponents() throws RmesException {

        logger.info("Getting all mutualized components");
        var components = repoGestion.getResponseAsArray(structureQueries.getComponents(true, true, true));
        return DiacriticSorter.sort(components, PartialStructureComponent[].class, PartialStructureComponent::labelLg1);
    }

    public JSONObject getComponentObject(String id) throws RmesException {

        logger.info("Starting to get one mutualized component");
        JSONArray response = repoGestion.getResponseAsArray(structureQueries.getComponent(id));

        if (response.isEmpty()) {
            throw new RmesNotFoundException("This component does not exist", id);
        }

        // We first format linked attributes if they exists
        JSONObject component = new JSONObject(response.getJSONObject(0).toMap());

        this.repoGestion.getMultipleTripletsForObject(
                component,
                "contributor",
                structureQueries.getComponentContributors(component.getString("component")),
                "contributor");
        component.remove("component");

        if (component.has(ATTRIBUTE_IRI)) {
            component.remove(ATTRIBUTE_IRI);
        }
        if (component.has(VALUE_IRI)) {
            component.remove(VALUE_IRI);
        }

        List<JSONObject> attributes = JSONUtils.stream(response)
                .filter(current -> current.has(ATTRIBUTE_IRI)
                        && current.has(VALUE_IRI)
                        && !current.getString(ATTRIBUTE_IRI).isEmpty()
                        && !current.getString(VALUE_IRI).isEmpty())
                .toList();
        IntStream.range(0, attributes.size()).forEach(index -> {
            component.put("attribute_" + index, attributes.get(index).getString(ATTRIBUTE_IRI));
            component.put("attributeValue_" + index, attributes.get(index).getString(VALUE_IRI));
        });

        return structureComponentRepository.formatComponent(id, component);
    }

    @Override
    public String getComponent(String id) throws RmesException {
        return this.getComponentObject(id).toString();
    }

    @Override
    public String updateComponent(String componentId, String body) throws RmesException {
        return structureComponentRepository.updateComponent(componentId, body);
    }

    @Override
    public String createComponent(String body) throws RmesException {
        return structureComponentRepository.createComponent(body);
    }

    @Override
    public void deleteComponent(String id) throws RmesException {
        JSONObject response = this.getComponentObject(id);
        if (response.keySet().isEmpty()) {
            throw new RmesNotFoundException("Not Found", "component with " + id + " not found");
        }
        String type = response.getString("type");
        structureComponentRepository.deleteComponent(response, id, type);
    }

    @Override
    public String publishComponent(String id) throws RmesException {
        return structureComponentRepository.publishComponent(this.getComponentObject(id));
    }
}
