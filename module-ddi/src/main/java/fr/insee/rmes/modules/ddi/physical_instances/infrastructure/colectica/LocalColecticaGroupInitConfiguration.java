package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DDIReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StringValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StudyUnitReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Title;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaItem;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaResponse;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.QueryRequest;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.AbstractColecticaItemRepository.generateDeterministicUuid;

/**
 * Configuration activated when {@code fr.insee.rmes.bauhaus.colectica.init=true}.
 * <p>
 * At application startup, this bean:
 * <ol>
 *   <li>Deprecates all existing groups from Colectica</li>
 *   <li>Queries GraphDB via SPARQL to fetch series and their operations</li>
 *   <li>Creates StudyUnits FIRST (so they exist with full content)</li>
 *   <li>Creates Groups with StudyUnitReferences (pointing to existing StudyUnits)</li>
 * </ol>
 * <p>
 * StudyUnits must be created before Groups. If Groups are created first,
 * Colectica auto-creates empty stubs for referenced StudyUnits at Version 1,
 * and {@code RegisterOrReplace} with Version 1 cannot overwrite those stubs.
 */
@Configuration
@ConditionalOnProperty(name = "fr.insee.rmes.bauhaus.colectica.init", havingValue = "true")
public class LocalColecticaGroupInitConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LocalColecticaGroupInitConfiguration.class);

    @Bean
    CommandLineRunner initColecticaGroups(
            GroupService groupService,
            StudyUnitService studyUnitService,
            DDIService ddiService,
            RepositoryGestion repositoryGestion,
            ColecticaConfiguration colecticaConfiguration,
            ColecticaAuthenticator colecticaAuthenticator,
            RestTemplate restTemplate,
            @Value("${fr.insee.rmes.bauhaus.baseGraph}") String baseGraph,
            @Value("${fr.insee.rmes.bauhaus.operations.graph}") String operationsGraph
    ) {
        return args -> {
            logger.info("=== Initializing Colectica groups and study units from SPARQL ===");

            String defaultAgencyId = colecticaConfiguration.server().defaultAgencyId();
            String defaultLang = colecticaConfiguration.langs().getFirst();
            String versionResponsibility = colecticaConfiguration.server().versionResponsibility();

            // Step 1: Deprecate all existing groups
            logger.info("Step 1: Deprecating all existing groups from Colectica");
            groupService.deprecateAll();

            // Step 2: Query GraphDB for series and operations
            logger.info("Step 2: Querying GraphDB for series and operations");
            String graphUri = baseGraph + operationsGraph;
            List<SeriesWithOperations> seriesData = querySeriesAndOperations(repositoryGestion, graphUri);
            logger.info("Found {} series", seriesData.size());

            // Step 3: Create study units and groups
            // Step 3a: Create PhysicalInstances and StudyUnits FIRST so they exist with full content
            // before Groups reference them. PhysicalInstances are created before StudyUnits so that
            // StudyUnits can embed a PhysicalInstanceReference pointing to the already-created PI.
            logger.info("Step 3a: Creating physical instances and study units in Colectica FIRST");
            int groupsCreated = 0;
            int studyUnitsCreated = 0;
            int physicalInstancesCreated = 0;

            for (SeriesWithOperations series : seriesData) {
                for (OperationInfo operation : series.operations()) {
                    try {
                        String studyUnitId = generateDeterministicUuid(operation.operationIri());
                        String studyUnitLabel = operation.operationLabel() + " Study Unit";
                        String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                                "true",
                                versionDate,
                                "urn:ddi:%s:%s:1".formatted(defaultAgencyId, studyUnitId),
                                defaultAgencyId,
                                studyUnitId,
                                "1",
                                new Citation(new Title(new StringValue(defaultLang, studyUnitLabel))),
                                operation.operationIri(),
                                null
                        );

                        logger.info("Creating study unit: operationId={}, uri={}, generatedUuid={}, label='{}'",
                                operation.operationId(), operation.operationIri(), studyUnitId, studyUnitLabel);
                        studyUnitService.createOrUpdate(studyUnit);
                        studyUnitsCreated++;
                        logger.info("Study unit created successfully: operationId={}", operation.operationId());

                        String physicalInstanceLabel = operation.operationLabel() + " Physical Instance";
                        logger.info("Creating physical instance: operationId={}, label='{}'", operation.operationId(), physicalInstanceLabel);
                        Ddi4Response piResponse = ddiService.createPhysicalInstance(new CreatePhysicalInstanceRequest(physicalInstanceLabel, physicalInstanceLabel, null, null, null, null, null));
                        Ddi4PhysicalInstance pi = piResponse.physicalInstance().getFirst();
                        studyUnitService.addPhysicalInstance(studyUnit, new DDIReference(pi.agency(), pi.id(), pi.version()));
                        physicalInstancesCreated++;
                        logger.info("Physical instance created and linked to study unit: operationId={}, piId={}", operation.operationId(), pi.id());
                    } catch (Exception e) {
                        logger.error("Failed to create study unit or physical instance for operation: id={}, uri={}", operation.operationId(), operation.operationIri(), e);
                    }
                }
            }

            // Step 3b: Create Groups AFTER StudyUnits
            // Groups contain StudyUnitReferences pointing to the StudyUnits created above.
            // Since the StudyUnits already exist, Colectica won't create empty stubs.
            logger.info("Step 3b: Creating groups in Colectica AFTER study units");

            for (SeriesWithOperations series : seriesData) {
                try {
                    String groupId = generateDeterministicUuid(series.seriesIri());
                    String groupLabel = series.seriesLabel() + " Group";
                    String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                    List<StudyUnitReference> studyUnitRefs = series.operations().stream()
                            .map(op -> new StudyUnitReference(
                                    defaultAgencyId,
                                    generateDeterministicUuid(op.operationIri()),
                                    "1",
                                    "StudyUnit"
                            ))
                            .toList();

                    Ddi4Group group = new Ddi4Group(
                            "true",
                            versionDate,
                            "urn:ddi:%s:%s:1".formatted(defaultAgencyId, groupId),
                            defaultAgencyId,
                            groupId,
                            "1",
                            versionResponsibility,
                            new Citation(new Title(new StringValue(defaultLang, groupLabel))),
                            studyUnitRefs,
                            List.of(series.seriesIri()),
                            "insee:StatisticalOperationSeries"
                    );

                    logger.info("Creating group: id={}, uri={}, operations={}", series.seriesId(), series.seriesIri(), series.operations().size());
                    groupService.createOrUpdate(group);
                    groupsCreated++;
                } catch (Exception e) {
                    logger.error("Failed to create group for series: id={}", series.seriesId(), e);
                }
            }

            logger.info("=== Colectica initialization complete: {} groups, {} study units, {} physical instances created ===", groupsCreated, studyUnitsCreated, physicalInstancesCreated);

            // Step 4: Verify items in Colectica by querying back
            logger.info("Step 4: Verifying created items in Colectica via _query");
            verifyItemsInColectica(colecticaAuthenticator, restTemplate, colecticaConfiguration);
        };
    }

    private void verifyItemsInColectica(ColecticaAuthenticator authenticator, RestTemplate restTemplate,
                                         ColecticaConfiguration colecticaConfiguration) {
        String groupItemType = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";
        String studyUnitItemType = "752a535b-b548-4fbe-97e4-f26a02d9e413";
        String queryUrl = colecticaConfiguration.server().baseApiUrl() + "_query";

        authenticator.executeWithAuth(token -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            // Query Groups
            try {
                QueryRequest groupQuery = new QueryRequest(List.of(groupItemType));
                HttpEntity<QueryRequest> groupRequest = new HttpEntity<>(groupQuery, headers);
                ColecticaResponse groupResponse = restTemplate.postForObject(queryUrl, groupRequest, ColecticaResponse.class);
                logger.info("=== VERIFICATION: Groups total={} ===", groupResponse != null ? groupResponse.totalResults() : "null");
                if (groupResponse != null && groupResponse.results() != null) {
                    int count = 0;
                    int withItem = 0;
                    int withLabel = 0;
                    for (var group : groupResponse.results()) {
                        if (group.item() != null) withItem++;
                        if (group.label() != null && !group.label().isEmpty()) withLabel++;
                        if (count < 3) {
                            logger.info("Group[{}]: id={}, label={}, versionDate={}, itemFormat={}, hasItem={}",
                                    count, group.identifier(), group.label(),
                                    group.versionDate(), group.itemFormat(), group.item() != null);
                        }
                        count++;
                    }
                    logger.info("=== Groups summary: total={}, withItem={}, withLabel={} ===", count, withItem, withLabel);
                }
            } catch (Exception e) {
                logger.error("Failed to verify groups", e);
            }

            // Query StudyUnits
            try {
                QueryRequest suQuery = new QueryRequest(List.of(studyUnitItemType));
                HttpEntity<QueryRequest> suRequest = new HttpEntity<>(suQuery, headers);
                ColecticaResponse suResponse = restTemplate.postForObject(queryUrl, suRequest, ColecticaResponse.class);
                logger.info("=== VERIFICATION: StudyUnits total={} ===", suResponse != null ? suResponse.totalResults() : "null");
                if (suResponse != null && suResponse.results() != null) {
                    int count = 0;
                    int withItem = 0;
                    int withLabel = 0;
                    for (var su : suResponse.results()) {
                        if (su.item() != null) withItem++;
                        if (su.label() != null && !su.label().isEmpty()) withLabel++;
                        if (count < 3) {
                            logger.info("StudyUnit[{}]: id={}, label={}, versionDate={}, itemFormat={}, hasItem={}",
                                    count, su.identifier(), su.label(),
                                    su.versionDate(), su.itemFormat(), su.item() != null);
                        }
                        count++;
                    }
                    logger.info("=== StudyUnits summary: total={}, withItem={}, withLabel={} ===", count, withItem, withLabel);
                }
            } catch (Exception e) {
                logger.error("Failed to verify study units", e);
            }

            return null;
        });
    }

    List<SeriesWithOperations> querySeriesAndOperations(RepositoryGestion repositoryGestion, String graphUri) throws RmesException {
        String sparql = FreeMarkerUtils.buildRequest("operations/", "getSeriesWithOperations.ftlh",
                Map.of("GRAPH_URI", graphUri));

        JSONArray results = repositoryGestion.getResponseAsArray(sparql);

        if (results == null) {
            return List.of();
        }

        // Group operations by series
        Map<String, SeriesBuilder> seriesMap = new HashMap<>();

        for (int i = 0; i < results.length(); i++) {
            JSONObject row = results.getJSONObject(i);
            String seriesId = row.getString("seriesId");
            String seriesIri = row.getString("seriesIri");
            String seriesLabel = row.optString("seriesLabel", seriesId);

            SeriesBuilder builder = seriesMap.computeIfAbsent(seriesId,
                    k -> new SeriesBuilder(seriesId, seriesIri, seriesLabel));

            if (row.has("operationId") && !row.isNull("operationId")) {
                String operationId = row.getString("operationId");
                String operationIri = row.getString("operationIri");
                String operationLabel = row.optString("operationLabel", operationId);
                if (!operationId.isEmpty()) {
                    builder.addOperation(new OperationInfo(operationId, operationIri, operationLabel));
                }
            }
        }

        return seriesMap.values().stream()
                .map(SeriesBuilder::build)
                .toList();
    }

    record SeriesWithOperations(String seriesId, String seriesIri, String seriesLabel, List<OperationInfo> operations) {}
    record OperationInfo(String operationId, String operationIri, String operationLabel) {}

    private static class SeriesBuilder {
        private final String seriesId;
        private final String seriesIri;
        private final String seriesLabel;
        private final List<OperationInfo> operations = new ArrayList<>();

        SeriesBuilder(String seriesId, String seriesIri, String seriesLabel) {
            this.seriesId = seriesId;
            this.seriesIri = seriesIri;
            this.seriesLabel = seriesLabel;
        }

        void addOperation(OperationInfo op) {
            operations.add(op);
        }

        SeriesWithOperations build() {
            return new SeriesWithOperations(seriesId, seriesIri, seriesLabel, List.copyOf(operations));
        }
    }
}
