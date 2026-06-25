package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariablesInRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.dto.ColecticaResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.AbstractColecticaItemRepository.generateDeterministicUuid;

/**
 * Configuration activated when {@code fr.insee.rmes.bauhaus.colectica.init=true}.
 * <p>
 * At application startup, this bean:
 * <ol>
 *   <li>Queries GraphDB via SPARQL to fetch series and their operations</li>
 *   <li>Deprecates ONLY the groups and study units this init manipulates (those derived from the
 *       series/operations read above, all variants included), leaving every other Colectica item untouched</li>
 *   <li>Creates StudyUnits FIRST (so they exist with full content)</li>
 *   <li>For each Group variant, creates an (initially empty) CodeListScheme and the LogicalProduct
 *       that files it, then the Group itself — with StudyUnitReferences (pointing to existing
 *       StudyUnits) and a LogicalProductReference, so each Group exposes the
 *       Group → LogicalProduct → CodeListScheme chain</li>
 * </ol>
 * <p>
 * For each operation it creates {@link #VARIANT_LABEL_WORDS}.size() StudyUnit variants (and one
 * PhysicalInstance each), and for each series the same number of Group variants — every variant
 * carrying a distinct, intentionally non-alphabetical label. This seeds enough differently-named
 * items to exercise the alphabetical ordering of the Group / StudyUnit listings.
 * <p>
 * StudyUnits must be created before Groups. If Groups are created first,
 * Colectica auto-creates empty stubs for referenced StudyUnits at Version 1,
 * and {@code RegisterOrReplace} with Version 1 cannot overwrite those stubs.
 */
@Configuration
@ConditionalOnProperty(name = "fr.insee.rmes.bauhaus.colectica.init", havingValue = "true")
public class LocalColecticaGroupInitConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LocalColecticaGroupInitConfiguration.class);

    /**
     * Mots de libellé des variantes créées pour chaque série (Groups) et chaque opération
     * (StudyUnits). Ils sont volontairement DANS UN ORDRE NON ALPHABÉTIQUE afin que le tri
     * alphabétique de la feature à tester réordonne effectivement les éléments : à la création
     * les libellés arrivent dans l'ordre Zoulou, Alpha, Mike, Bravo, Yankee. Leur nombre fixe le
     * nombre de variantes générées par série et par opération.
     */
    static final List<String> VARIANT_LABEL_WORDS = List.of("Zoulou", "Alpha", "Mike", "Bravo", "Yankee");

    private static final String VARIANT_SEED_SEPARATOR = "#variant-";

    /**
     * Suffixes appended to a group variant's seed to derive the deterministic ids of its
     * LogicalProduct and (initially empty) CodeListScheme. Each group variant owns one of each,
     * exposing the Group → LogicalProduct → CodeListScheme chain.
     */
    private static final String LOGICAL_PRODUCT_SEED_SUFFIX = "#logicalproduct";
    private static final String CODE_LIST_SCHEME_SEED_SUFFIX = "#codelistscheme";

    /**
     * Les ids Colectica déterministes des {@link #VARIANT_LABEL_WORDS} variantes dérivées d'une IRI.
     * La même formule de graine sert à la création (étapes 3a/3b) et à la dépréciation (étape 2),
     * pour que les deux portent exactement sur les mêmes objets.
     */
    static List<String> variantUuids(String iri) {
        List<String> ids = new ArrayList<>();
        for (int variant = 0; variant < VARIANT_LABEL_WORDS.size(); variant++) {
            ids.add(generateDeterministicUuid(iri + VARIANT_SEED_SEPARATOR + variant));
        }
        return ids;
    }

    @Bean
    CommandLineRunner initColecticaGroups(
            GroupService groupService,
            StudyUnitService studyUnitService,
            DDIService ddiService,
            RepositoryPublicationReader repositoryPublicationReader,
            ColecticaConfiguration colecticaConfiguration,
            ColecticaClient colecticaClient,
            @Value("${fr.insee.rmes.bauhaus.baseGraph}") String baseGraph,
            @Value("${fr.insee.rmes.bauhaus.operations.graph}") String operationsGraph
    ) {
        return args -> {
            logger.info("=== Initializing Colectica groups and study units from SPARQL ===");

            String defaultAgencyId = colecticaConfiguration.server().defaultAgencyId();
            String defaultLang = colecticaConfiguration.langs().getFirst();
            String versionResponsibility = colecticaConfiguration.server().versionResponsibility();

            // Step 1: Query the publication GraphDB repository for series and operations.
            // On lit le dépôt de publication (et non gestion) pour que les IRIs récupérées soient
            // en base de publication (http://id.insee.fr/...), donc cohérentes avec la clé de
            // recherche de l'endpoint GET /ddi/operation/{id}/studyUnit.
            logger.info("Step 1: Querying publication GraphDB for series and operations");
            String graphUri = baseGraph + operationsGraph;
            List<SeriesWithOperations> seriesData = querySeriesAndOperations(repositoryPublicationReader, graphUri);
            logger.info("Found {} series", seriesData.size());

            // Step 2: Deprecate ONLY the groups and study units this init manipulates.
            // Les ids sont les mêmes ids déterministes (dérivés de l'IRI) que ceux utilisés à la
            // création (étapes 3a/3b) : on ne touche donc qu'aux objets que l'init va recréer, et
            // jamais aux Groups/StudyUnits d'autres séries présents dans Colectica.
            Set<String> manipulatedGroupIds = seriesData.stream()
                    .flatMap(series -> variantUuids(series.seriesIri()).stream())
                    .collect(Collectors.toSet());
            Set<String> manipulatedStudyUnitIds = seriesData.stream()
                    .flatMap(series -> series.operations().stream())
                    .flatMap(operation -> variantUuids(operation.operationIri()).stream())
                    .collect(Collectors.toSet());
            logger.info("Step 2: Deprecating {} manipulated group(s) and {} manipulated study unit(s)",
                    manipulatedGroupIds.size(), manipulatedStudyUnitIds.size());
            groupService.deprecate(manipulatedGroupIds);
            studyUnitService.deprecate(manipulatedStudyUnitIds);

            // Vue d'ensemble : pour chaque série / operation, le Group / StudyUnit « logique » associé
            // (1 série → 1 groupe, 1 operation → 1 study unit). Les étapes 3a/3b créent ensuite
            // VARIANT_LABEL_WORDS.size() variantes de chacun, aux libellés distincts.
            logDdiAssociations(buildDdiAssociations(seriesData), defaultAgencyId);

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
                    // Une StudyUnit (et sa PhysicalInstance) par variante, aux libellés distincts.
                    for (int variant = 0; variant < VARIANT_LABEL_WORDS.size(); variant++) {
                        String variantWord = VARIANT_LABEL_WORDS.get(variant);
                        try {
                            String studyUnitId = generateDeterministicUuid(operation.operationIri() + VARIANT_SEED_SEPARATOR + variant);
                            String studyUnitLabel = operation.operationLabel() + " " + variantWord + " Study Unit";
                            String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                            Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                                    Ddi4StudyUnit.TYPE,
                                    CogsDate.ofDateTime(versionDate),
                                    "urn:ddi:%s:%s:1".formatted(defaultAgencyId, studyUnitId),
                                    defaultAgencyId,
                                    studyUnitId,
                                    "1",
                                    new Citation(LangStrings.of(defaultLang, studyUnitLabel)),
                                    operation.operationIri(),
                                    null
                            );

                            logger.info("Creating study unit: operationId={}, uri={}, variant={}, generatedUuid={}, label='{}'",
                                    operation.operationId(), operation.operationIri(), variantWord, studyUnitId, studyUnitLabel);
                            studyUnitService.createOrUpdate(studyUnit);
                            studyUnitsCreated++;
                            logger.info("Study unit created successfully: operationId={}, variant={}", operation.operationId(), variantWord);

                            String physicalInstanceLabel = operation.operationLabel() + " " + variantWord + " Physical Instance";
                            logger.info("Creating physical instance: operationId={}, label='{}'", operation.operationId(), physicalInstanceLabel);
                            Ddi4Response piResponse = ddiService.createPhysicalInstance(new CreatePhysicalInstanceRequest(physicalInstanceLabel, physicalInstanceLabel, null, null, null, null, null));
                            Ddi4PhysicalInstance pi = piResponse.physicalInstance().getFirst();
                            studyUnitService.addPhysicalInstance(studyUnit, Reference.of(pi.agency(), pi.id(), pi.version(), "PhysicalInstance"));
                            physicalInstancesCreated++;
                            logger.info("Physical instance created and linked to study unit: operationId={}, variant={}, piId={}", operation.operationId(), variantWord, pi.id());
                        } catch (Exception e) {
                            logger.error("Failed to create study unit or physical instance for operation: id={}, uri={}, variant={}", operation.operationId(), operation.operationIri(), variantWord, e);
                        }
                    }
                }
            }

            // Step 3b: Create Groups AFTER StudyUnits
            // Groups contain StudyUnitReferences pointing to the StudyUnits created above.
            // Since the StudyUnits already exist, Colectica won't create empty stubs.
            logger.info("Step 3b: Creating groups in Colectica AFTER study units");

            for (SeriesWithOperations series : seriesData) {
                // Un Group par variante, aux libellés distincts, référençant la StudyUnit de même variante.
                for (int variant = 0; variant < VARIANT_LABEL_WORDS.size(); variant++) {
                    String variantWord = VARIANT_LABEL_WORDS.get(variant);
                    try {
                        String groupId = generateDeterministicUuid(series.seriesIri() + VARIANT_SEED_SEPARATOR + variant);
                        String groupLabel = series.seriesLabel() + " " + variantWord + " Group";
                        String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                        // Create the group's (initially empty) CodeListScheme and the LogicalProduct that
                        // files it, BEFORE the group, so Colectica does not auto-create empty stubs for the
                        // items the group references (same ordering constraint as study units above).
                        String codeListSchemeId = generateDeterministicUuid(
                                series.seriesIri() + VARIANT_SEED_SEPARATOR + variant + CODE_LIST_SCHEME_SEED_SUFFIX);
                        Ddi4CodeListScheme codeListScheme = new Ddi4CodeListScheme(
                                Ddi4CodeListScheme.TYPE,
                                CogsDate.ofDateTime(versionDate),
                                "urn:ddi:%s:%s:1".formatted(defaultAgencyId, codeListSchemeId),
                                defaultAgencyId,
                                codeListSchemeId,
                                "1",
                                LangStrings.of(defaultLang, groupLabel + " Code List Scheme"),
                                List.of()
                        );
                        logger.info("Creating code list scheme: id={}, variant={}", codeListSchemeId, variantWord);
                        ddiService.createCodeListScheme(codeListScheme);

                        String logicalProductId = generateDeterministicUuid(
                                series.seriesIri() + VARIANT_SEED_SEPARATOR + variant + LOGICAL_PRODUCT_SEED_SUFFIX);
                        Ddi4LogicalProduct logicalProduct = new Ddi4LogicalProduct(
                                Ddi4LogicalProduct.TYPE,
                                CogsDate.ofDateTime(versionDate),
                                "urn:ddi:%s:%s:1".formatted(defaultAgencyId, logicalProductId),
                                defaultAgencyId,
                                logicalProductId,
                                "1",
                                LangStrings.of(defaultLang, groupLabel + " Logical Product"),
                                List.of(Reference.of(defaultAgencyId, codeListSchemeId, "1", "CodeListScheme"))
                        );
                        logger.info("Creating logical product: id={}, variant={}, codeListScheme={}", logicalProductId, variantWord, codeListSchemeId);
                        ddiService.createLogicalProduct(logicalProduct);

                        final int currentVariant = variant;
                        List<Reference> studyUnitRefs = series.operations().stream()
                                .map(op -> Reference.of(
                                        defaultAgencyId,
                                        generateDeterministicUuid(op.operationIri() + VARIANT_SEED_SEPARATOR + currentVariant),
                                        "1",
                                        "StudyUnit"
                                ))
                                .toList();

                        Ddi4Group group = new Ddi4Group(
                                Ddi4Group.TYPE,
                                CogsDate.ofDateTime(versionDate),
                                "urn:ddi:%s:%s:1".formatted(defaultAgencyId, groupId),
                                defaultAgencyId,
                                groupId,
                                "1",
                                versionResponsibility,
                                new Citation(LangStrings.of(defaultLang, groupLabel)),
                                studyUnitRefs,
                                List.of(series.seriesIri()),
                                "insee:StatisticalOperationSeries",
                                List.of(Reference.of(defaultAgencyId, logicalProductId, "1", "LogicalProduct"))
                        );

                        logger.info("Creating group: id={}, uri={}, variant={}, label='{}', operations={}",
                                series.seriesId(), series.seriesIri(), variantWord, groupLabel, series.operations().size());
                        groupService.createOrUpdate(group);
                        groupsCreated++;
                    } catch (Exception e) {
                        logger.error("Failed to create group for series: id={}, variant={}", series.seriesId(), variantWord, e);
                    }
                }
            }

            logger.info("=== Colectica initialization complete: {} groups, {} study units, {} physical instances created ===", groupsCreated, studyUnitsCreated, physicalInstancesCreated);

            // Step 3c: Create an example PhysicalInstance whose Code variable references a code list
            // that does not exist. Ce cas ne devrait jamais arriver en conditions normales, mais il
            // permet de reproduire / valider le message d'erreur explicite côté front (la variable X
            // référence la liste de codes Y qui n'existe pas).
            logger.info("Step 3c: Creating example PhysicalInstance with a variable referencing a missing code list");
            createMissingCodeListExample(ddiService, defaultAgencyId, defaultLang);

            // Step 4: Verify items in Colectica by querying back
            logger.info("Step 4: Verifying created items in Colectica via _query");
            verifyItemsInColectica(colecticaClient);
        };
    }

    private void verifyItemsInColectica(ColecticaClient colecticaClient) {
        String groupItemType = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";
        String studyUnitItemType = "752a535b-b548-4fbe-97e4-f26a02d9e413";

        // Query Groups
        try {
            ColecticaResponse groupResponse = colecticaClient.query(List.of(groupItemType));
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
                ColecticaResponse suResponse = colecticaClient.query(List.of(studyUnitItemType));
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
    }

    /**
     * Crée une PhysicalInstance d'exemple contenant une unique variable « Code » dont la
     * {@code CodeListReference} pointe vers une liste de codes inexistante (id bidon). Sert à
     * reproduire le cas « variable associée à une liste de codes qui n'existe pas » : à l'ouverture
     * de la variable, le front affiche un message d'erreur explicite avec les agency/id de la
     * variable et de la liste de codes manquante.
     *
     * <p>La variable est rattachée au {@code LogicalRecord} de la PI (via {@code VariablesInRecord})
     * pour faire partie du set, et aucune {@code CodeList} n'est fournie dans la réponse : la
     * référence pointe donc volontairement dans le vide.
     */
    private void createMissingCodeListExample(DDIService ddiService, String defaultAgencyId, String defaultLang) {
        try {
            String label = "EXEMPLE - variable avec liste de codes inexistante";
            Ddi4Response created = ddiService.createPhysicalInstance(
                    new CreatePhysicalInstanceRequest(label, label, null, null, null, null, null));

            Ddi4PhysicalInstance pi = created.physicalInstance().getFirst();
            Ddi4DataRelationship dataRelationship = created.dataRelationship().getFirst();
            LogicalRecord logicalRecord = dataRelationship.logicalRecord().getFirst();

            String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String variableId = generateDeterministicUuid("example:variable:missing-code-list");
            // Liste de codes qui n'existe pas : un UUID volontairement absent de Colectica.
            Reference missingCodeListRef = Reference.of(
                    defaultAgencyId, "00000000-0000-0000-0000-000000000000", "1", "CodeList");

            Ddi4Variable variable = new Ddi4Variable(
                    Ddi4Variable.TYPE,
                    CogsDate.ofDateTime(versionDate),
                    "urn:ddi:%s:%s:1".formatted(defaultAgencyId, variableId),
                    defaultAgencyId, variableId, "1",
                    null,
                    LangStrings.of(defaultLang, "VAR_CL_FANTOME"),
                    LangStrings.of(defaultLang, "Variable référençant une liste de codes inexistante"),
                    null,
                    new VariableRepresentation(
                            null,
                            new CodeRepresentation(CodeRepresentation.TYPE, Boolean.FALSE, missingCodeListRef),
                            null, null, null),
                    null);

            LogicalRecord updatedLogicalRecord = new LogicalRecord(
                    LogicalRecord.TYPE,
                    logicalRecord.urn(), logicalRecord.agency(), logicalRecord.id(),
                    logicalRecord.version(), logicalRecord.label(),
                    new VariablesInRecord(List.of(
                            Reference.of(defaultAgencyId, variableId, "1", "Variable"))));

            Ddi4DataRelationship updatedDataRelationship = new Ddi4DataRelationship(
                    Ddi4DataRelationship.TYPE,
                    dataRelationship.versionDate(), dataRelationship.urn(), dataRelationship.agency(),
                    dataRelationship.id(), dataRelationship.version(), dataRelationship.basedOnObject(),
                    dataRelationship.label(), List.of(updatedLogicalRecord));

            Ddi4Response full = new Ddi4Response(
                    Ddi4Response.SCHEMA,
                    created.topLevelReference(),
                    List.of(pi),
                    List.of(updatedDataRelationship),
                    List.of(variable),
                    null,  // pas de CodeList : la référence de la variable pointe dans le vide
                    null);

            ddiService.updateFullPhysicalInstance(pi.agency(), pi.id(), full);
            logger.info("Example PhysicalInstance with a missing code list created: {}/{} (variable {} -> code list {}/00000000-0000-0000-0000-000000000000)",
                    pi.agency(), pi.id(), variableId, defaultAgencyId);
        } catch (Exception e) {
            logger.error("Failed to create the example PhysicalInstance with a missing code list", e);
        }
    }

    List<SeriesWithOperations> querySeriesAndOperations(RepositoryPublicationReader repositoryPublicationReader, String graphUri) throws RmesException {
        String sparql = FreeMarkerUtils.buildRequest("operations/", "getSeriesWithOperations.ftlh",
                Map.of("GRAPH_URI", graphUri));

        JSONArray results = repositoryPublicationReader.getResponseAsArray(sparql);

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

    record SeriesDdiAssociation(SeriesWithOperations series, String groupId, List<OperationDdiAssociation> operations) {}
    record OperationDdiAssociation(OperationInfo operation, String studyUnitId) {}

    /**
     * Associe chaque série à son Group DDI et chaque operation à sa StudyUnit DDI, via l'id
     * déterministe {@link AbstractColecticaItemRepository#generateDeterministicUuid(String)}
     * (le même que celui utilisé à la création). Méthode pure, sans effet de bord.
     */
    List<SeriesDdiAssociation> buildDdiAssociations(List<SeriesWithOperations> seriesData) {
        return seriesData.stream()
                .map(series -> new SeriesDdiAssociation(
                        series,
                        generateDeterministicUuid(series.seriesIri()),
                        series.operations().stream()
                                .map(op -> new OperationDdiAssociation(op, generateDeterministicUuid(op.operationIri())))
                                .toList()))
                .toList();
    }

    private void logDdiAssociations(List<SeriesDdiAssociation> associations, String defaultAgencyId) {
        logger.info("=== Series and operations associated with DDI objects: {} series ===", associations.size());
        for (SeriesDdiAssociation association : associations) {
            SeriesWithOperations series = association.series();
            logger.info("Series '{}' (id={}, iri={}) -> DDI Group {}:{} | {} operation(s)",
                    series.seriesLabel(), series.seriesId(), series.seriesIri(),
                    defaultAgencyId, association.groupId(), association.operations().size());
            for (OperationDdiAssociation op : association.operations()) {
                OperationInfo operation = op.operation();
                logger.info("    Operation '{}' (id={}, iri={}) -> DDI StudyUnit {}:{}",
                        operation.operationLabel(), operation.operationId(), operation.operationIri(),
                        defaultAgencyId, op.studyUnitId());
            }
        }
    }

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
