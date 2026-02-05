package fr.insee.rmes.modules.users.infrastructure.stamps;

import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.datasets.datasets.infrastructure.DatasetQueries;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@ServerSideAdaptor
@Repository
public class GraphDbStampChecker implements StampChecker {
    private final RepositoryGestion repositoryGestion;
    private final DatasetQueries datasetQueries;

    public GraphDbStampChecker(
            RepositoryGestion repositoryGestion,
            DatasetQueries datasetQueries) {
        this.repositoryGestion = repositoryGestion;
        this.datasetQueries = datasetQueries;
    }


    @Override
    public List<String> getCreatorsStamps(RBAC.Module module, String id) throws UnsupportedModuleException, StampFetchException {
        try {
        var query = switch (module) {
            case OPERATION_SERIES -> {
                var iri = RdfUtils.objectIRI(ObjectType.SERIES, id);

                    yield OperationSeriesQueries.getCreatorsBySeriesUri(iri.toString());

            }
            default -> throw new UnsupportedModuleException(module);
        };

        return this.getStamps("creators", query);

        } catch (RmesException e) {
            throw new StampFetchException(module, id);
        }
    }

    @Override
    public List<String> getContributorsStamps(RBAC.Module module, String id) throws UnsupportedModuleException, StampFetchException {
        try {
            var query = switch (module) {
                case STRUCTURE_STRUCTURE -> {
                    var iri = RdfUtils.objectIRI(ObjectType.STRUCTURE, id);
                    yield StructureQueries.getContributorsByStructureUri(iri.toString());
                }
                case STRUCTURE_COMPONENT -> {
                    var iri = findStructureComponentIri(id);
                    yield StructureQueries.getContributorsByComponentUri(iri.toString());
                }
                case DATASET_DISTRIBUTION -> {
                    var iri = RdfUtils.objectIRI(ObjectType.DISTRIBUTION, id);
                    yield DatasetDistributionQueries.getContributorsByDistributionUri(iri.toString());
                }
                case DATASET_DATASET -> {
                    var iri = RdfUtils.objectIRI(ObjectType.DATASET, id);
                    yield datasetQueries.getDatasetContributors(iri);
                }
                default -> throw new UnsupportedModuleException(module);
            };
            return this.getStamps("contributors", query);
        } catch(RmesException e){
            throw new StampFetchException(module, id);
        }
    }

    private IRI findStructureComponentIri(String componentId) throws RmesException {
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

    private List<String> getStamps(String key, String query) {

        try {
            JSONArray contributors = this.repositoryGestion.getResponseAsArray(query);
            List<String> stamps = new ArrayList<>();
            for (int i = 0; i < contributors.length(); i++) {
                JSONObject obj = contributors.getJSONObject(i);
                if (obj.has(key)) {
                    stamps.add(obj.getString(key));
                }
            }
            return stamps;
        } catch (RmesException e) {
            throw new RuntimeException(e);
        }
    }
}