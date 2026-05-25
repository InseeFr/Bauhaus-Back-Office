package fr.insee.rmes.modules.users.infrastructure.stamps;

import fr.insee.rmes.domain.model.OrganisationOption;
import fr.insee.rmes.domain.port.clientside.OrganisationService;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.datasets.datasets.infrastructure.DatasetQueries;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ServerSideAdaptor
@Repository
public class GraphDbStampChecker implements StampChecker {
    private static final Logger logger = LoggerFactory.getLogger(GraphDbStampChecker.class);

    private final RepositoryGestion repositoryGestion;
    private final DatasetQueries datasetQueries;
    private final StructureQueries structureQueries;
    private final OperationSeriesQueries operationSeriesQueries;
    private final DDIRepository ddiRepository;
    private final SeriesCreatorsPort seriesCreatorsPort;
    private final DatasetDistributionQueries datasetDistributionQueries;
    private final OrganisationService organisationService;

    public GraphDbStampChecker(
            RepositoryGestion repositoryGestion,
            DatasetQueries datasetQueries,
            StructureQueries structureQueries,
            OperationSeriesQueries operationSeriesQueries,
            DDIRepository ddiRepository,
            SeriesCreatorsPort seriesCreatorsPort,
            DatasetDistributionQueries datasetDistributionQueries,
            OrganisationService organisationService) {
        this.repositoryGestion = repositoryGestion;
        this.datasetQueries = datasetQueries;
        this.structureQueries = structureQueries;
        this.operationSeriesQueries = operationSeriesQueries;
        this.ddiRepository = ddiRepository;
        this.seriesCreatorsPort = seriesCreatorsPort;
        this.datasetDistributionQueries = datasetDistributionQueries;
        this.organisationService = organisationService;
    }


    @Override
    public List<String> getCreatorsStamps(RBAC.Module module, String id) throws UnsupportedModuleException, StampFetchException {
        try {
            return switch (module) {
                case OPERATION_SERIES -> {
                    var iri = RdfUtils.objectIRI(ObjectType.SERIES, id);
                    yield normalizeOrganisationStamps(this.getStamps("creators", operationSeriesQueries.getCreatorsBySeriesUri(iri.toString())));
                }
                case DDI_PHYSICALINSTANCE -> {
                    if (id == null) yield List.of();
                    String[] parts = id.split("\\|", 2);
                    if (parts.length < 2) yield List.of();
                    String agency = parts[0];
                    String groupId = parts[1];
                    Ddi4GroupResponse groupResponse = ddiRepository.getGroup(agency, groupId);
                    List<String> seriesIris = groupResponse.group() == null ? List.of() :
                            groupResponse.group().stream()
                                    .filter(g -> g.seriesIris() != null)
                                    .flatMap(g -> g.seriesIris().stream())
                                    .toList();
                    if (seriesIris.isEmpty()) yield List.of();
                    Map<String, List<String>> creatorsByIri = seriesCreatorsPort.getCreatorsForSeries(seriesIris);
                    List<String> distinctCreators = creatorsByIri.values().stream()
                            .flatMap(Collection::stream)
                            .distinct()
                            .toList();
                    yield normalizeOrganisationStamps(distinctCreators).stream().distinct().toList();
                }
                default -> throw new UnsupportedModuleException(module);
            };
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
                    yield structureQueries.getContributorsByStructureUri(iri.toString());
                }
                case STRUCTURE_COMPONENT -> {
                    var iri = findStructureComponentIri(id);
                    yield structureQueries.getContributorsByComponentUri(iri.toString());
                }
                case DATASET_DISTRIBUTION -> {
                    var iri = RdfUtils.objectIRI(ObjectType.DISTRIBUTION, id);
                    yield datasetDistributionQueries.getContributorsByDistributionUri(iri.toString());
                }
                case DATASET_DATASET -> {
                    var iri = RdfUtils.objectIRI(ObjectType.DATASET, id);
                    yield datasetQueries.getDatasetContributors(iri);
                }
                default -> throw new UnsupportedModuleException(module);
            };
            return normalizeOrganisationStamps(this.getStamps("contributors", query));
        } catch(RmesException e){
            throw new StampFetchException(module, id);
        }
    }

    private List<String> normalizeOrganisationStamps(List<String> values) {
        if (values == null) return List.of();
        if (values.isEmpty()) return values;
        Map<String, OrganisationOption> organisationsMap;
        try {
            organisationsMap = organisationService.getOrganisationsMap(values.stream().distinct().toList());
        } catch (RmesException e) {
            logger.warn("Failed to resolve organisation stamps; keeping raw values", e);
            return values;
        }
        if (organisationsMap == null || organisationsMap.isEmpty()) {
            return values;
        }
        List<String> normalized = new ArrayList<>(values.size());
        for (String value : values) {
            OrganisationOption option = organisationsMap.get(value);
            if (option != null && option.stamp() != null && !option.stamp().isBlank()) {
                normalized.add(option.stamp());
            } else {
                normalized.add(value);
            }
        }
        return Collections.unmodifiableList(normalized);
    }

    private IRI findStructureComponentIri(String componentId) throws RmesException {
        JSONObject type = repositoryGestion.getResponseAsObject(structureQueries.getComponentType(componentId));
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