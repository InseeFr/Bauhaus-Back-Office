package fr.insee.rmes.modules.ddi.physical_instances.domain.services;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DDIServiceImpl implements DDIService {
    static final Logger logger = LoggerFactory.getLogger(DDIServiceImpl.class);

    private final DDIRepository ddiRepository;
    private final SeriesCreatorsPort seriesCreatorsPort;

    public DDIServiceImpl(DDIRepository ddiRepository, SeriesCreatorsPort seriesCreatorsPort) {
        this.ddiRepository = ddiRepository;
        this.seriesCreatorsPort = seriesCreatorsPort;
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
                .filter(instance -> stampsByGroupKey
                        .getOrDefault(groupKeyByInstance.get(instance), List.of())
                        .stream().anyMatch(userStamps::contains))
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
                    List<String> stamps = stampsByGroupKey.computeIfAbsent(groupKey,
                            _ -> resolveGroupCreatorStamps(row.groupAgency(), row.groupId()));
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
        logger.info("Starting to get logical products for group {}/{}", agencyId, groupId);
        return ddiRepository.getLogicalProductsByGroup(agencyId, groupId);
    }

    @Override
    public List<PartialCodeListScheme> getCodeListSchemes() {
        logger.info("Starting to get code list schemes list");
        return ddiRepository.getCodeListSchemes();
    }

    @Override
    public List<PartialCodeListScheme> getCodeListSchemesByLogicalProduct(String agencyId, String logicalProductId) {
        logger.info("Starting to get code list schemes for logical product {}/{}", agencyId, logicalProductId);
        return ddiRepository.getCodeListSchemesByLogicalProduct(agencyId, logicalProductId);
    }

    @Override
    public List<PartialCodesList> getCodeListsByCodeListScheme(String agencyId, String codeListSchemeId) {
        logger.info("Starting to get code lists for code list scheme {}/{}", agencyId, codeListSchemeId);
        return ddiRepository.getCodeListsByCodeListScheme(agencyId, codeListSchemeId);
    }

    @Override
    public List<PartialCodesList> getCodeListsByGroup(String agencyId, String groupId) {
        logger.info("Starting to get all code lists for group {}/{} (all logical products / code list schemes)", agencyId, groupId);

        // Group -> LogicalProduct -> CodeListScheme -> CodeList, agrégé et dédupliqué par agency/id.
        Map<String, PartialCodesList> codeListsByKey = new LinkedHashMap<>();
        for (PartialLogicalProduct logicalProduct : ddiRepository.getLogicalProductsByGroup(agencyId, groupId)) {
            for (PartialCodeListScheme scheme : ddiRepository.getCodeListSchemesByLogicalProduct(logicalProduct.agency(), logicalProduct.id())) {
                for (PartialCodesList codeList : ddiRepository.getCodeListsByCodeListScheme(scheme.agency(), scheme.id())) {
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
        logger.info("Starting to get missing (sentinel) code lists for group {}/{}", agencyId, groupId);
        return ddiRepository.getMissingCodesListsByGroup(agencyId, groupId);
    }

    @Override
    public List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId) {
        logger.info("Starting to get variables using code list {}/{}", codeListAgencyId, codeListId);
        return ddiRepository.getVariablesUsingCodeList(codeListAgencyId, codeListId);
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

        Set<String> allSeriesIris = allGroups.stream()
                .flatMap(g -> g.seriesIris().stream())
                .collect(Collectors.toSet());

        Map<String, List<String>> creatorsByIri = seriesCreatorsPort.getCreatorsForSeries(allSeriesIris);

        return allGroups.stream()
                .filter(group -> group.seriesIris().stream()
                        .anyMatch(iri -> {
                            List<String> creators = creatorsByIri.getOrDefault(iri, List.of());
                            return creators.stream().anyMatch(userStamps::contains);
                        }))
                .sorted(LabelComparators.byLabelAscending(PartialGroup::label))
                .toList();
    }

    @Override
    public Ddi4GroupResponse getDdi4Group(String agencyId, String id) {
        Ddi4GroupResponse response = ddiRepository.getGroup(agencyId, id);
        if (response == null || response.studyUnit() == null) {
            return response;
        }
        List<Ddi4StudyUnit> sortedStudyUnits = response.studyUnit().stream()
                .sorted(LabelComparators.byLabelDescending(DDIServiceImpl::studyUnitLabel))
                .toList();
        return new Ddi4GroupResponse(response.schema(), response.topLevelReference(),
                response.group(), sortedStudyUnits);
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
        if (response == null || response.variable() == null) {
            return response;
        }
        // Tri par défaut des variables sur le nom (VariableName), ascendant.
        List<Ddi4Variable> sortedVariables = response.variable().stream()
                .sorted(LabelComparators.byLabelAscending(DDIServiceImpl::variableName))
                .toList();
        return new Ddi4Response(response.schema(), response.topLevelReference(),
                response.physicalInstance(), response.dataRelationship(), sortedVariables,
                response.codeList(), response.category());
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
    public Ddi4Response updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response) {
        ddiRepository.updateFullPhysicalInstance(agencyId, id, ddi4Response);
        return ddiRepository.getPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request) {
        logger.info("Creating new physical instance with label: {}", request.physicalInstanceLabel());
        return ddiRepository.createPhysicalInstance(request);
    }

    @Override
    public void createLogicalProduct(Ddi4LogicalProduct logicalProduct) {
        logger.info("Creating logical product: {}/{}", logicalProduct.agency(), logicalProduct.id());
        ddiRepository.createLogicalProduct(logicalProduct);
    }

    @Override
    public void createCodeListScheme(Ddi4CodeListScheme codeListScheme) {
        logger.info("Creating code list scheme: {}/{}", codeListScheme.agency(), codeListScheme.id());
        ddiRepository.createCodeListScheme(codeListScheme);
    }

    @Override
    public void createCategoryScheme(Ddi4CategoryScheme categoryScheme) {
        logger.info("Creating category scheme: {}/{}", categoryScheme.agency(), categoryScheme.id());
        ddiRepository.createCategoryScheme(categoryScheme);
    }

    @Override
    public void createVariableScheme(Ddi4VariableScheme variableScheme) {
        logger.info("Creating variable scheme: {}/{}", variableScheme.agency(), variableScheme.id());
        ddiRepository.createVariableScheme(variableScheme);
    }

    @Override
    public void createManagedRepresentationScheme(Ddi4ManagedRepresentationScheme managedRepresentationScheme) {
        logger.info("Creating managed representation scheme: {}/{}",
                managedRepresentationScheme.agency(), managedRepresentationScheme.id());
        ddiRepository.createManagedRepresentationScheme(managedRepresentationScheme);
    }

    @Override
    public void createManagedMissingValuesRepresentation(Ddi4ManagedMissingValuesRepresentation managedMissingValuesRepresentation) {
        logger.info("Creating managed missing values representation: {}/{}",
                managedMissingValuesRepresentation.agency(), managedMissingValuesRepresentation.id());
        ddiRepository.createManagedMissingValuesRepresentation(managedMissingValuesRepresentation);
    }

    @Override
    public void createCodeList(Ddi4CodeList codeList) {
        logger.info("Creating code list: {}/{}", codeList.agency(), codeList.id());
        ddiRepository.createCodeList(codeList);
    }

    @Override
    public void createCategory(Ddi4Category category) {
        logger.info("Creating category: {}/{}", category.agency(), category.id());
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
    public Ddi4Response getMutualizedCodesList(String agencyId, String id) {
        logger.info("Getting mutualized codes list {}/{}", agencyId, id);
        return ddiRepository.getMutualizedCodesList(agencyId, id);
    }

    @Override
    public Ddi4Response getCodeList(String agencyId, String id, String version) {
        logger.info("Getting code list {}/{}/{}", agencyId, id, version);
        return ddiRepository.getCodeList(agencyId, id, version);
    }

    @Override
    public String getCodeListXml(String agencyId, String id, String version) {
        logger.info("Getting code list DDI 3.3 XML {}/{}/{}", agencyId, id, version);
        return ddiRepository.getCodeListXml(agencyId, id, version);
    }

    @Override
    public Ddi4Response getDataRelationships(String agencyId, String id, String version) {
        logger.info("Getting data relationships {}/{}/{}", agencyId, id, version);
        return ddiRepository.getDataRelationships(agencyId, id, version);
    }

    @Override
    public String getDataRelationshipsXml(String agencyId, String id, String version) {
        logger.info("Getting data relationships DDI 3.3 XML {}/{}/{}", agencyId, id, version);
        return ddiRepository.getDataRelationshipsXml(agencyId, id, version);
    }

    @Override
    public String getItemXml(String agency, String id, String version) {
        logger.info("Getting DDI 3.3 XML for {}/{}/{}", agency, id, version);
        return ddiRepository.getItemXml(agency, id, version);
    }

    @Override
    public String getItemXml(String agency, String id) {
        logger.info("Getting DDI 3.3 XML (latest version) for {}/{}", agency, id);
        return ddiRepository.getItemXml(agency, id);
    }

    @Override
    public PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id) {
        logger.info("Getting parents for physical instance {}/{}", agencyId, id);
        PhysicalInstanceParents parents = ddiRepository.getPhysicalInstanceParents(agencyId, id);
        // Un seul appel Colectica pour le groupe parent : il sert à la fois au label
        // affiché (section « groupe » du sélecteur de listes de codes) et aux stamps créateurs.
        Ddi4GroupResponse groupResponse = ddiRepository.getGroup(parents.groupAgency(), parents.groupId());
        return parents
                .withGroupLabel(extractGroupLabel(groupResponse))
                .withStudyUnitLabel(extractStudyUnitLabel(groupResponse, parents.studyUnitId()))
                .withStamps(resolveGroupCreatorStamps(groupResponse));
    }

    /**
     * Libellé de l'étude (StudyUnit) rattachée à la PI : le groupe parent files ses study units,
     * on retrouve celle de la PI par son ID dans cette même réponse (aucun appel Colectica en plus),
     * ou {@code null} si elle n'y figure pas ou n'a pas de titre.
     */
    private String extractStudyUnitLabel(Ddi4GroupResponse groupResponse, String studyUnitId) {
        if (groupResponse == null || groupResponse.studyUnit() == null || studyUnitId == null) {
            return null;
        }
        return groupResponse.studyUnit().stream()
                .filter(studyUnit -> studyUnitId.equals(studyUnit.id()))
                .map(DDIServiceImpl::studyUnitLabel)
                .filter(label -> label != null && !label.isBlank())
                .findFirst()
                .orElse(null);
    }

    /**
     * Libellé du groupe parent : premier titre de la {@code Citation} disponible
     * (peu importe la langue), ou {@code null} si le groupe n'a pas de titre.
     */
    private String extractGroupLabel(Ddi4GroupResponse groupResponse) {
        if (groupResponse == null || groupResponse.group() == null) {
            return null;
        }
        return groupResponse.group().stream()
                .map(Ddi4Group::citation)
                .filter(citation -> citation != null && citation.title() != null && !citation.title().isEmpty())
                .map(citation -> citation.title().getFirst().value())
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
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
                        .distinct()
                        .toList();
        if (seriesIris.isEmpty()) {
            return List.of();
        }
        return seriesCreatorsPort.getCreatorsForSeries(seriesIris).values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

    @Override
    public Optional<String> getStudyUnitXmlByOperationIri(String operationIri) {
        logger.info("Getting StudyUnit XML by operationIri: {}", operationIri);
        return ddiRepository.findStudyUnitXmlByOperationIri(operationIri);
    }
}