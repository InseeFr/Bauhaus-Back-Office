package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import static fr.insee.rmes.domain.logging.LogSanitizer.forLog;

import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.DdiItemNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidSentinelValuesException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CategoryCodeListUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnitResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DuplicatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.MutualizedCodeListCodes;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceIds;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.operation.operations.domain.port.serverside.OperationIrisPort;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDIServiceImpl implements DDIService {
    static final Logger logger = LoggerFactory.getLogger(DDIServiceImpl.class);

    private final DDIRepository ddiRepository;
    private final SeriesCreatorsPort seriesCreatorsPort;
    private final OperationIrisPort operationIrisPort;
    private final Clock clock;

    public DDIServiceImpl(
            DDIRepository ddiRepository,
            SeriesCreatorsPort seriesCreatorsPort,
            OperationIrisPort operationIrisPort,
            Clock clock) {
        this.ddiRepository = ddiRepository;
        this.seriesCreatorsPort = seriesCreatorsPort;
        this.operationIrisPort = operationIrisPort;
        this.clock = clock;
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info("Starting to get physical instances list");
        return ddiRepository.getPhysicalInstancesViaAdvancedQuery().stream()
                .sorted(LabelComparators.byLabelAscending(PartialPhysicalInstance::label))
                .toList();
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstancesFilteredByStamp(Set<String> userStamps) {
        logger.info("Starting to get physical instances filtered by stamp");
        List<PartialPhysicalInstance> allInstances = ddiRepository.getPhysicalInstancesViaAdvancedQuery();

        // PI -> clé "agency|id" du groupe parent (résolution Colectica, par PI)
        Map<PartialPhysicalInstance, String> groupKeyByInstance = new LinkedHashMap<>();
        for (PartialPhysicalInstance instance : allInstances) {
            PhysicalInstanceParents parents =
                    ddiRepository.getPhysicalInstanceParents(instance.agency(), instance.id());
            groupKeyByInstance.put(instance, parents.groupAgency() + "|" + parents.groupId());
        }

        // stamps créateurs résolus une seule fois par groupe distinct (et non par PI)
        Map<String, List<String>> stampsByGroupKey = new HashMap<>();
        for (String groupKey : Set.copyOf(groupKeyByInstance.values())) {
            String[] parts = groupKey.split("\\|", 2);
            stampsByGroupKey.put(groupKey, resolveGroupCreatorStamps(parts[0], parts[1]));
        }

        return allInstances.stream()
                .filter(instance -> stampsByGroupKey.getOrDefault(groupKeyByInstance.get(instance), List.of()).stream()
                        .anyMatch(userStamps::contains))
                .sorted(LabelComparators.byLabelAscending(PartialPhysicalInstance::label))
                .toList();
    }

    @Override
    public List<PhysicalInstanceSearchRow> searchPhysicalInstances() {
        logger.info("Starting advanced search of physical instances (joining study unit and group labels)");
        return ddiRepository.getPhysicalInstanceSearchRows().stream()
                .sorted(LabelComparators.byLabelAscending(PhysicalInstanceSearchRow::label))
                .toList();
    }

    @Override
    public List<PhysicalInstanceSearchRow> searchPhysicalInstancesFilteredByStamp(Set<String> userStamps) {
        logger.info("Starting advanced search of physical instances filtered by stamp");
        // stamps créateurs résolus une seule fois par groupe distinct (et non par PI)
        Map<String, List<String>> stampsByGroupKey = new HashMap<>();
        return searchPhysicalInstances().stream()
                .filter(row -> {
                    if (row.groupId() == null) {
                        return false;
                    }
                    String groupKey = row.groupAgency() + "|" + row.groupId();
                    List<String> stamps = stampsByGroupKey.computeIfAbsent(
                            groupKey, _ -> resolveGroupCreatorStamps(row.groupAgency(), row.groupId()));
                    return stamps.stream().anyMatch(userStamps::contains);
                })
                .toList();
    }

    @Override
    public List<PartialLogicalProduct> getLogicalProducts() {
        logger.info("Starting to get logical products list");
        return ddiRepository.getLogicalProducts();
    }

    @Override
    public List<PartialLogicalProduct> getLogicalProductsByGroup(String agencyId, String groupId) {
        logger.info("Starting to get logical products for group {}/{}", forLog(agencyId), forLog(groupId));
        return ddiRepository.getLogicalProductsByGroup(agencyId, groupId);
    }

    @Override
    public List<PartialCodeListScheme> getCodeListSchemes() {
        logger.info("Starting to get code list schemes list");
        return ddiRepository.getCodeListSchemes();
    }

    @Override
    public List<PartialCodeListScheme> getCodeListSchemesByLogicalProduct(String agencyId, String logicalProductId) {
        logger.info(
                "Starting to get code list schemes for logical product {}/{}",
                forLog(agencyId),
                forLog(logicalProductId));
        return ddiRepository.getCodeListSchemesByLogicalProduct(agencyId, logicalProductId);
    }

    @Override
    public List<PartialCodesList> getCodeListsByCodeListScheme(String agencyId, String codeListSchemeId) {
        logger.info(
                "Starting to get code lists for code list scheme {}/{}", forLog(agencyId), forLog(codeListSchemeId));
        return ddiRepository.getCodeListsByCodeListScheme(agencyId, codeListSchemeId);
    }

    @Override
    public List<PartialCodesList> getCodeListsByGroup(String agencyId, String groupId) {
        logger.info(
                "Starting to get all code lists for group {}/{} (all logical products / code list schemes)",
                forLog(agencyId),
                forLog(groupId));

        // Group -> LogicalProduct -> CodeListScheme -> CodeList, agrégé et dédupliqué par agency/id.
        Map<String, PartialCodesList> codeListsByKey = new LinkedHashMap<>();
        for (PartialLogicalProduct logicalProduct : ddiRepository.getLogicalProductsByGroup(agencyId, groupId)) {
            for (PartialCodeListScheme scheme :
                    ddiRepository.getCodeListSchemesByLogicalProduct(logicalProduct.agency(), logicalProduct.id())) {
                for (PartialCodesList codeList :
                        ddiRepository.getCodeListsByCodeListScheme(scheme.agency(), scheme.id())) {
                    codeListsByKey.putIfAbsent(codeList.agency() + "|" + codeList.id(), codeList);
                }
            }
        }
        // Les listes de valeurs sentinelles (cf. #1566) sont classées dans les mêmes CodeListSchemes
        // mais exposées par getMissingCodesListsByGroup : on les exclut des listes « classiques ».
        for (PartialCodesList sentinelCodeList : ddiRepository.getMissingCodesListsByGroup(agencyId, groupId)) {
            codeListsByKey.remove(sentinelCodeList.agency() + "|" + sentinelCodeList.id());
        }
        return List.copyOf(codeListsByKey.values());
    }

    @Override
    public List<PartialCodesList> getMissingCodesListsByGroup(String agencyId, String groupId) {
        logger.info("Starting to get missing (sentinel) code lists for group {}/{}", forLog(agencyId), forLog(groupId));
        return ddiRepository.getMissingCodesListsByGroup(agencyId, groupId);
    }

    @Override
    public List<PartialMissingValuesRepresentation> getMissingValuesRepresentationsByGroup(
            String agencyId, String groupId) {
        logger.info(
                "Starting to get reusable missing values representations for group {}/{}",
                forLog(agencyId),
                forLog(groupId));
        return ddiRepository.getMissingValuesRepresentationsByGroup(agencyId, groupId);
    }

    @Override
    public List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId) {
        logger.info("Starting to get variables using code list {}/{}", forLog(codeListAgencyId), forLog(codeListId));
        return ddiRepository.getVariablesUsingCodeList(codeListAgencyId, codeListId);
    }

    @Override
    public List<CategoryCodeListUsage> getCodeListsUsingCategory(String categoryAgencyId, String categoryId) {
        logger.info("Starting to get code lists using category {}/{}", forLog(categoryAgencyId), forLog(categoryId));
        return ddiRepository.getCodeListsUsingCategory(categoryAgencyId, categoryId);
    }

    @Override
    public List<CodeListVariableUsage> getVariablesUsingMissingValuesRepresentation(String agencyId, String mmvrId) {
        logger.info(
                "Starting to get variables using missing values representation {}/{}",
                forLog(agencyId),
                forLog(mmvrId));
        return ddiRepository.getVariablesUsingMissingValuesRepresentation(agencyId, mmvrId);
    }

    @Override
    public Ddi4Response getStudyUnitVariables(String agencyId, String studyUnitId) {
        logger.info("Starting to get variables of study unit {}/{}", forLog(agencyId), forLog(studyUnitId));
        return ddiRepository.getStudyUnitVariables(agencyId, studyUnitId);
    }

    @Override
    public List<CodeListVariableUsage> getStudyUnitVariableUsages(String agencyId, String studyUnitId) {
        logger.info("Starting to get variable usages of study unit {}/{}", forLog(agencyId), forLog(studyUnitId));
        return ddiRepository.getStudyUnitVariableUsages(agencyId, studyUnitId);
    }

    @Override
    public List<PartialGroup> getGroups() {
        logger.info("Starting to get groups list");
        return ddiRepository.getGroups().stream()
                .sorted(LabelComparators.byLabelAscending(PartialGroup::label))
                .toList();
    }

    @Override
    public List<PartialGroup> getGroupsFilteredByStamp(Set<String> userStamps) {
        logger.info("Starting to get groups filtered by stamp");
        List<PartialGroup> allGroups = ddiRepository.getGroups();

        Set<String> allSeriesIris =
                allGroups.stream().flatMap(g -> g.seriesIris().stream()).collect(Collectors.toSet());

        Map<String, List<String>> creatorsByIri = seriesCreatorsPort.getCreatorsForSeries(allSeriesIris);

        return allGroups.stream()
                .filter(group -> group.seriesIris().stream().anyMatch(iri -> {
                    List<String> creators = creatorsByIri.getOrDefault(iri, List.of());
                    return creators.stream().anyMatch(userStamps::contains);
                }))
                .sorted(LabelComparators.byLabelAscending(PartialGroup::label))
                .toList();
    }

    @Override
    public Ddi4GroupResponse getDdi4Group(String agencyId, String id) {
        Ddi4GroupResponse response = ddiRepository.getGroup(agencyId, id);
        if (response == null) {
            throw DdiItemNotFoundException.group(agencyId, id);
        }
        if (response.studyUnit() == null) {
            return response;
        }
        List<Ddi4StudyUnit> sortedStudyUnits = response.studyUnit().stream()
                .sorted(LabelComparators.byLabelDescending(DDIServiceImpl::studyUnitLabel))
                .toList();
        return new Ddi4GroupResponse(
                response.schema(), response.topLevelReference(), response.group(), sortedStudyUnits);
    }

    private static String studyUnitLabel(Ddi4StudyUnit studyUnit) {
        Citation citation = studyUnit.citation();
        if (citation == null || citation.title() == null || citation.title().isEmpty()) {
            return "";
        }
        return citation.title().getFirst().value();
    }

    @Override
    public Ddi4Response getDdi4PhysicalInstance(String agencyId, String id) {
        Ddi4Response response = this.ddiRepository.getPhysicalInstance(agencyId, id);
        if (response == null) {
            throw DdiItemNotFoundException.physicalInstance(agencyId, id);
        }
        if (response.variable() == null) {
            return response;
        }
        // Tri par défaut des variables sur le nom (VariableName), ascendant.
        List<Ddi4Variable> sortedVariables = response.variable().stream()
                .sorted(LabelComparators.byLabelAscending(DDIServiceImpl::variableName))
                .toList();
        return new Ddi4Response(
                response.schema(),
                response.topLevelReference(),
                response.physicalInstance(),
                response.dataRelationship(),
                sortedVariables,
                response.codeList(),
                response.category(),
                response.managedMissingValuesRepresentation());
    }

    private static String variableName(Ddi4Variable variable) {
        List<LangString> names = variable.variableName();
        if (names == null || names.isEmpty()) {
            return "";
        }
        return names.getFirst().value();
    }

    @Override
    public List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id) {
        return ddiRepository.getPhysicalInstanceCodeLists(agencyId, id);
    }

    @Override
    public Ddi4Response updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request) {
        ddiRepository.updatePhysicalInstance(agencyId, id, request);
        return ddiRepository.getPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response duplicatePhysicalInstance(
            String agencyId, String id, DuplicatePhysicalInstanceRequest request) {
        logger.info(
                "Duplicating physical instance {}/{} with label: {}",
                forLog(agencyId),
                forLog(id),
                forLog(request.physicalInstanceLabel()));
        Reference copy = ddiRepository.duplicatePhysicalInstance(agencyId, id, request);
        return ddiRepository.getPhysicalInstance(copy.agency(), copy.id());
    }

    @Override
    public void updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response) {
        validateSentinelValues(ddi4Response);
        // Lecture préalable de l'état stocké : les items non modifiés gardent leur date stockée, les
        // items modifiés ou nouveaux passent à « maintenant », avec propagation enfant → parent.
        // Seuls les items du payload sont relus (listes de codes et catégories comprises, sans quoi
        // elles passeraient pour nouvelles), et des items qu'il référence seulement, rien que la
        // version : relire le set complet de la PI coûtait des dizaines de secondes.
        Ddi4Response current = ddiRepository.getStoredItems(ddi4Response);
        List<Reference> referenced = ddiRepository.getLatestVersions(StoredVersions.references(ddi4Response));
        // Versions alignées sur l'état stocké avant la comparaison des contenus : la v1 émise par le
        // front ferait sinon passer chaque item en v2+ pour modifié.
        Ddi4Response reconciled = VersionDateReconciler.reconcile(
                current,
                StoredVersions.align(current, referenced, ddi4Response),
                CogsDate.ofDateTime(ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        ddiRepository.updateFullPhysicalInstance(agencyId, id, reconciled);
    }

    /**
     * Valeurs sentinelles (#1566) : la carte rend les labels obligatoires sur la MMVR et sur sa
     * CodeList de sentinelles — un payload qui les omet est rejeté avant toute écriture.
     */
    private static void validateSentinelValues(Ddi4Response ddi4Response) {
        List<Ddi4ManagedMissingValuesRepresentation> mmvrs = ddi4Response.managedMissingValuesRepresentation();
        if (mmvrs == null) {
            return;
        }
        Map<String, Ddi4CodeList> codeListsByKey = new HashMap<>();
        for (Ddi4CodeList codeList :
                ddi4Response.codeList() != null ? ddi4Response.codeList() : List.<Ddi4CodeList>of()) {
            codeListsByKey.put(codeList.agency() + "/" + codeList.id(), codeList);
        }
        for (Ddi4ManagedMissingValuesRepresentation mmvr : mmvrs) {
            if (hasNoLabel(mmvr.label())) {
                throw InvalidSentinelValuesException.missingRepresentationLabel(mmvr.agency(), mmvr.id());
            }
            for (CodeRepresentation rep : mmvr.missingCodeRepresentation() != null
                    ? mmvr.missingCodeRepresentation()
                    : List.<CodeRepresentation>of()) {
                Reference codeListRef = rep.codeListReference();
                if (codeListRef == null) {
                    continue;
                }
                Ddi4CodeList sentinelCodeList = codeListsByKey.get(codeListRef.agency() + "/" + codeListRef.id());
                if (sentinelCodeList != null && hasNoLabel(sentinelCodeList.label())) {
                    throw InvalidSentinelValuesException.missingCodeListLabel(
                            sentinelCodeList.agency(), sentinelCodeList.id());
                }
            }
        }
    }

    private static boolean hasNoLabel(List<LangString> label) {
        return label == null
                || label.stream()
                        .noneMatch(
                                entry -> entry.value() != null && !entry.value().isBlank());
    }

    @Override
    public Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request) {
        logger.info("Creating new physical instance with label: {}", forLog(request.physicalInstanceLabel()));
        return ddiRepository.createPhysicalInstance(request);
    }

    @Override
    public Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request, PhysicalInstanceIds ids) {
        logger.info(
                "Creating new physical instance with label: {} and imposed id: {}",
                forLog(request.physicalInstanceLabel()),
                forLog(ids.physicalInstance()));
        return ddiRepository.createPhysicalInstance(request, ids);
    }

    @Override
    public void createLogicalProduct(Ddi4LogicalProduct logicalProduct) {
        logger.info("Creating logical product: {}/{}", forLog(logicalProduct.agency()), forLog(logicalProduct.id()));
        ddiRepository.createLogicalProduct(logicalProduct);
    }

    @Override
    public void createCodeListScheme(Ddi4CodeListScheme codeListScheme) {
        logger.info("Creating code list scheme: {}/{}", forLog(codeListScheme.agency()), forLog(codeListScheme.id()));
        ddiRepository.createCodeListScheme(codeListScheme);
    }

    @Override
    public void createCategoryScheme(Ddi4CategoryScheme categoryScheme) {
        logger.info("Creating category scheme: {}/{}", forLog(categoryScheme.agency()), forLog(categoryScheme.id()));
        ddiRepository.createCategoryScheme(categoryScheme);
    }

    @Override
    public void createVariableScheme(Ddi4VariableScheme variableScheme) {
        logger.info("Creating variable scheme: {}/{}", forLog(variableScheme.agency()), forLog(variableScheme.id()));
        ddiRepository.createVariableScheme(variableScheme);
    }

    @Override
    public void createManagedRepresentationScheme(Ddi4ManagedRepresentationScheme managedRepresentationScheme) {
        logger.info(
                "Creating managed representation scheme: {}/{}",
                forLog(managedRepresentationScheme.agency()),
                forLog(managedRepresentationScheme.id()));
        ddiRepository.createManagedRepresentationScheme(managedRepresentationScheme);
    }

    @Override
    public void createManagedMissingValuesRepresentation(
            Ddi4ManagedMissingValuesRepresentation managedMissingValuesRepresentation) {
        logger.info(
                "Creating managed missing values representation: {}/{}",
                forLog(managedMissingValuesRepresentation.agency()),
                forLog(managedMissingValuesRepresentation.id()));
        ddiRepository.createManagedMissingValuesRepresentation(managedMissingValuesRepresentation);
    }

    @Override
    public void createCodeList(Ddi4CodeList codeList) {
        logger.info("Creating code list: {}/{}", forLog(codeList.agency()), forLog(codeList.id()));
        ddiRepository.createCodeList(codeList);
    }

    @Override
    public void createCategory(Ddi4Category category) {
        logger.info("Creating category: {}/{}", forLog(category.agency()), forLog(category.id()));
        ddiRepository.createCategory(category);
    }

    @Override
    public List<PartialCodesList> getMutualizedCodesLists() {
        logger.info("Starting to get mutualized codes lists");
        return ddiRepository.getMutualizedCodesLists();
    }

    @Override
    public void evictMutualizedCodesListsCache() {
        logger.info("Evicting mutualized codes lists cache");
        ddiRepository.evictMutualizedCodesListsCache();
    }

    @Override
    public void evictPhysicalInstanceSearchRowsCache() {
        logger.info("Evicting physical instance search rows cache");
        ddiRepository.evictPhysicalInstanceSearchRowsCache();
    }

    @Override
    public void evictAllCaches() {
        logger.info("Evicting all Colectica caches");
        ddiRepository.evictAllCaches();
    }

    @Override
    public MutualizedCodeListCodes getMutualizedCodeListCodes(String agencyId, String id) {
        logger.info("Getting codes of mutualized codes list {}/{}", forLog(agencyId), forLog(id));
        return ddiRepository.getMutualizedCodeListCodes(agencyId, id);
    }

    @Override
    public Ddi4Response getMutualizedCodesList(String agencyId, String id) {
        logger.info("Getting mutualized codes list {}/{}", forLog(agencyId), forLog(id));
        return ddiRepository.getMutualizedCodesList(agencyId, id);
    }

    @Override
    public Ddi4Response getCodeList(String agencyId, String id, String version) {
        logger.info("Getting code list {}/{}/{}", forLog(agencyId), forLog(id), forLog(version));
        return ddiRepository.getCodeList(agencyId, id, version);
    }

    @Override
    public String getCodeListXml(String agencyId, String id, String version) {
        logger.info("Getting code list DDI 3.3 XML {}/{}/{}", forLog(agencyId), forLog(id), forLog(version));
        return ddiRepository.getCodeListXml(agencyId, id, version);
    }

    @Override
    public Ddi4Response getDataRelationships(String agencyId, String id, String version) {
        logger.info("Getting data relationships {}/{}/{}", forLog(agencyId), forLog(id), forLog(version));
        return ddiRepository.getDataRelationships(agencyId, id, version);
    }

    @Override
    public String getDataRelationshipsXml(String agencyId, String id, String version) {
        logger.info("Getting data relationships DDI 3.3 XML {}/{}/{}", forLog(agencyId), forLog(id), forLog(version));
        return ddiRepository.getDataRelationshipsXml(agencyId, id, version);
    }

    @Override
    public String getItemXml(String agency, String id, String version) {
        logger.info("Getting DDI 3.3 XML for {}/{}/{}", forLog(agency), forLog(id), forLog(version));
        return ddiRepository.getItemXml(agency, id, version);
    }

    @Override
    public String getItemXml(String agency, String id) {
        logger.info("Getting DDI 3.3 XML (latest version) for {}/{}", forLog(agency), forLog(id));
        return ddiRepository.getItemXml(agency, id);
    }

    @Override
    public PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id) {
        logger.info("Getting parents for physical instance {}/{}", forLog(agencyId), forLog(id));
        // Les libellés (groupe, étude) arrivent avec la remontée des relations ; les stamps
        // créateurs et la série reflétée demandent de lire le groupe, et encore : ses seules IRIs
        // de séries. L'opération reflétée se lit de même sur la seule étude.
        PhysicalInstanceParents parents = ddiRepository.getPhysicalInstanceParents(agencyId, id);
        List<String> seriesIris = ddiRepository.getGroupSeriesIris(parents.groupAgency(), parents.groupId());
        String operationIri = ddiRepository
                .getStudyUnitOperationIri(parents.studyUnitAgency(), parents.studyUnitId())
                .orElse(null);
        return parents.resolved(
                creatorStampsOfSeries(seriesIris), seriesIris.isEmpty() ? null : seriesIris.getFirst(), operationIri);
    }

    private List<String> resolveGroupCreatorStamps(String groupAgency, String groupId) {
        return resolveGroupCreatorStamps(ddiRepository.getGroup(groupAgency, groupId));
    }

    /**
     * Stamps STAMP d'une instance physique : créateurs des séries du groupe parent.
     * Même résolution groupe → séries → créateurs que {@code getGroupsFilteredByStamp}
     * et que {@code GraphDbStampChecker.getCreatorsStamps} pour DDI_PHYSICALINSTANCE.
     */
    private List<String> resolveGroupCreatorStamps(Ddi4GroupResponse groupResponse) {
        List<String> seriesIris = groupResponse == null || groupResponse.group() == null
                ? List.of()
                : groupResponse.group().stream()
                        .filter(g -> g.seriesIris() != null)
                        .flatMap(g -> g.seriesIris().stream())
                        .toList();
        return creatorStampsOfSeries(seriesIris);
    }

    /** Stamps créateurs (distincts) des séries données, via GraphDB ; vide sans série. */
    private List<String> creatorStampsOfSeries(List<String> seriesIris) {
        List<String> distinctSeriesIris = seriesIris.stream().distinct().toList();
        if (distinctSeriesIris.isEmpty()) {
            return List.of();
        }
        return seriesCreatorsPort.getCreatorsForSeries(distinctSeriesIris).values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

    @Override
    public Optional<String> getStudyUnitXmlByOperationIri(String operationIri) {
        logger.info("Getting StudyUnit XML by operationIri: {}", forLog(operationIri));
        return ddiRepository.findStudyUnitXmlByOperationIri(operationIri);
    }

    @Override
    public Optional<Ddi4StudyUnitResponse> getStudyUnitByOperationIri(String operationIri) {
        logger.info("Getting StudyUnit DDI4 by operationIri: {}", forLog(operationIri));
        return ddiRepository.findStudyUnitByOperationIri(operationIri);
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstancesByOperation(String operationId) {
        logger.info("Getting physical instances by operation: {}", forLog(operationId));
        return ddiRepository.findPhysicalInstancesByOperationIris(operationIrisPort.irisOf(operationId));
    }
}
