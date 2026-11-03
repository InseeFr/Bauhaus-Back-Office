package fr.insee.rmes.modules.users.infrastructure.stamps;

import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb.GraphDBCollectionProperties;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationRepository;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@ServerSideAdaptor
@Repository
public class GraphDbStampChecker implements StampChecker {
    private static final Logger logger = LoggerFactory.getLogger(GraphDbStampChecker.class);

    private final RepositoryGestion repositoryGestion;
    private final StructureQueries structureQueries;
    private final OperationSeriesQueries operationSeriesQueries;
    private final DDIRepository ddiRepository;
    private final SeriesCreatorsPort seriesCreatorsPort;
    private final DatasetDistributionQueries datasetDistributionQueries;
    private final OrganisationRepository organisationRepository;
    private final ContributorsQueries contributorsQueries;
    private final ClassificationsQueries classificationsQueries;
    private final CodeListsQueries codeListsQueries;
    private final GraphsProperties graphs;
    private final GraphDBCollectionProperties collectionProperties;
    private final String datasetsGraph;
    private final String catalogRecordBaseUri;

    public GraphDbStampChecker(
            RepositoryGestion repositoryGestion,
            StructureQueries structureQueries,
            OperationSeriesQueries operationSeriesQueries,
            DDIRepository ddiRepository,
            SeriesCreatorsPort seriesCreatorsPort,
            DatasetDistributionQueries datasetDistributionQueries,
            OrganisationRepository organisationRepository,
            ContributorsQueries contributorsQueries,
            ClassificationsQueries classificationsQueries,
            CodeListsQueries codeListsQueries,
            GraphsProperties graphs,
            GraphDBCollectionProperties collectionProperties,
            @Value("${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.datasets.graph}") String datasetsGraph,
            @Value("${fr.insee.rmes.bauhaus.sesame.gestion.baseURI}/${fr.insee.rmes.bauhaus.datasets.record.baseURI}")
                    String catalogRecordBaseUri) {
        this.repositoryGestion = repositoryGestion;
        this.structureQueries = structureQueries;
        this.operationSeriesQueries = operationSeriesQueries;
        this.ddiRepository = ddiRepository;
        this.seriesCreatorsPort = seriesCreatorsPort;
        this.datasetDistributionQueries = datasetDistributionQueries;
        this.organisationRepository = organisationRepository;
        this.contributorsQueries = contributorsQueries;
        this.classificationsQueries = classificationsQueries;
        this.codeListsQueries = codeListsQueries;
        this.graphs = graphs;
        this.collectionProperties = collectionProperties;
        this.datasetsGraph = datasetsGraph;
        this.catalogRecordBaseUri = catalogRecordBaseUri;
    }

    @Override
    public List<String> getCreatorsStamps(RBAC.Module module, String id)
            throws UnsupportedModuleException, StampFetchException {
        try {
            return switch (module) {
                case OPERATION_SERIES -> {
                    var iri = RdfUtils.objectIRI(ObjectType.SERIES, id);
                    yield normalizeOrganisationStamps(
                            this.getStamps("creators", operationSeriesQueries.getCreatorsBySeriesUri(iri.toString())));
                }
                case DDI_PHYSICALINSTANCE -> {
                    if (id == null) yield List.of();
                    String[] parts = id.split("\\|", 2);
                    if (parts.length < 2) yield List.of();
                    String agency = parts[0];
                    String groupId = parts[1];
                    Ddi4GroupResponse groupResponse = ddiRepository.getGroup(agency, groupId);
                    List<String> seriesIris = groupResponse.group() == null
                            ? List.of()
                            : groupResponse.group().stream()
                                    .filter(g -> g.seriesIris() != null)
                                    .flatMap(g -> g.seriesIris().stream())
                                    .toList();
                    if (seriesIris.isEmpty()) yield List.of();
                    Map<String, List<String>> creatorsByIri = seriesCreatorsPort.getCreatorsForSeries(seriesIris);
                    List<String> distinctCreators = creatorsByIri.values().stream()
                            .flatMap(Collection::stream)
                            .distinct()
                            .toList();
                    yield normalizeOrganisationStamps(distinctCreators).stream()
                            .distinct()
                            .toList();
                }
                default -> throw new UnsupportedModuleException(module);
            };
        } catch (RmesException | IllegalArgumentException _) {
            // IllegalArgumentException : l'identifiant ne donne pas une IRI injectable dans la requête.
            throw new StampFetchException(module, id);
        }
    }

    @Override
    public List<String> getContributorsStamps(RBAC.Module module, String id)
            throws UnsupportedModuleException, StampFetchException {
        try {
            var query =
                    switch (module) {
                        case STRUCTURE_STRUCTURE ->
                            contributorsQueries.getContributors(
                                    graphs.structuresGraph(), RdfUtils.objectIRI(ObjectType.STRUCTURE, id));
                        case STRUCTURE_COMPONENT ->
                            contributorsQueries.getContributors(
                                    graphs.structuresComponentsGraph(), findStructureComponentIri(id));
                        // Le contributeur d'un jeu de données est porté par son entrée de catalogue.
                        case DATASET_DATASET ->
                            contributorsQueries.getContributors(
                                    datasetsGraph, RdfUtils.createIRI(catalogRecordBaseUri + "/" + id));
                        case CLASSIFICATION_CLASSIFICATION -> classificationContributorsQuery(id);
                        case CODESLIST_CODESLIST, CODESLIST_PARTIALCODESLIST -> codesListContributorsQuery(id);
                        case CONCEPT_CONCEPT ->
                            contributorsQueries.getContributors(
                                    graphs.conceptsGraph(), RdfUtils.objectIRI(ObjectType.CONCEPT, id));
                        case CONCEPT_COLLECTION ->
                            contributorsQueries.getContributors(
                                    graphs.conceptsGraph(), collectionProperties.getResourceIRI(id));
                        // Celui d'une distribution aussi, atteinte par jointure depuis la distribution.
                        case DATASET_DISTRIBUTION -> {
                            var iri = RdfUtils.objectIRI(ObjectType.DISTRIBUTION, id);
                            yield datasetDistributionQueries.getContributorsByDistributionUri(iri.toString());
                        }
                        default -> throw new UnsupportedModuleException(module);
                    };
            return normalizeOrganisationStamps(this.getStamps(module, query));
        } catch (RmesException | IllegalArgumentException _) {
            // IllegalArgumentException : l'identifiant ne donne pas une IRI injectable dans la requête.
            throw new StampFetchException(module, id);
        }
    }

    /** Une nomenclature vit dans son propre graphe, découvert avec son IRI. */
    private String classificationContributorsQuery(String id) throws RmesException {
        JSONObject graphAndUri = repositoryGestion.getResponseAsObject(classificationsQueries.getGraphUriById(id));
        if (graphAndUri.isEmpty()) {
            throw new IllegalArgumentException("Unknown classification " + id);
        }
        return contributorsQueries.getContributors(
                graphAndUri.getString("graph"), RdfUtils.createIRI(graphAndUri.getString("uri")));
    }

    /**
     * Une liste de codes, complète ou partielle, est adressée par sa notation : son IRI, identité
     * stable, est relue en base.
     */
    private String codesListContributorsQuery(String notation) throws RmesException {
        JSONObject codesList =
                repositoryGestion.getResponseAsObject(codeListsQueries.getDetailedCodeListByNotation(notation));
        if (!codesList.has("iri")) {
            throw new IllegalArgumentException("Unknown codes list " + notation);
        }
        return contributorsQueries.getContributors(
                graphs.codeListGraph(), RdfUtils.createIRI(codesList.getString("iri")));
    }

    private List<String> getStamps(RBAC.Module module, String query) {
        return getStamps(module == RBAC.Module.DATASET_DISTRIBUTION ? "contributors" : "contributor", query);
    }

    private List<String> normalizeOrganisationStamps(List<String> values) {
        if (values == null) return List.of();
        if (values.isEmpty()) return values;
        Map<String, OrganisationOption> organisationsMap;
        try {
            organisationsMap = organisationRepository.getOrganisationsMap(
                    values.stream().distinct().toList());
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
            JSONUtils.stream(contributors).forEach(obj -> {
                if (obj.has(key)) {
                    stamps.add(obj.getString(key));
                }
            });
            return stamps;
        } catch (RmesException e) {
            throw new RuntimeException(e);
        }
    }
}
