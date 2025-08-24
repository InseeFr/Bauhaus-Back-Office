package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StructureComponentStampChecker implements  ObjectStampChecker {

    private final RepositoryGestion repositoryGestion;


    public StructureComponentStampChecker(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }

    @Override
    public List<String> getStamps(String id) {

        try {
            IRI iri = findComponentIRI(id);

            JSONArray contributors = this.repositoryGestion.getResponseAsArray(StructureQueries.getContributorsByComponentUri(iri.toString()));
            List<String> stamps = new ArrayList<>();
            for (int i = 0; i < contributors.length(); i++) {
                JSONObject obj = contributors.getJSONObject(i);
                if (obj.has("contributors")) {
                    stamps.add(obj.getString("contributors"));
                }
            }
            return stamps;
        } catch (RmesException e) {
            return Collections.emptyList();
        }
    }

    private IRI findComponentIRI(String componentId) throws RmesException {
        JSONObject type = repositoryGestion.getResponseAsObject(StructureQueries.getComponentType(componentId));
        String componentType = type.getString("type");
        if (componentType.equals(RdfUtils.toString(QB.ATTRIBUTE_PROPERTY))) {
            return RdfUtils.structureComponentAttributeIRI(componentId);
        } else if (componentType.equals(RdfUtils.toString(QB.DIMENSION_PROPERTY))) {
            return RdfUtils.structureComponentDimensionIRI(componentId);
        } else {
            return RdfUtils.structureComponentMeasureIRI(componentId);
        }
    }
}