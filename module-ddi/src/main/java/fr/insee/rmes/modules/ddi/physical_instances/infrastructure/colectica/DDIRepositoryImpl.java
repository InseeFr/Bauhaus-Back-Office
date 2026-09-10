package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CategoryCodeListUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
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
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnitResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * Adaptateur Colectica du port {@link DDIRepository}.
 *
 * <p>La classe n'implémente aucune logique propre : elle assemble les collaborateurs de l'adaptateur
 * et leur délègue chaque opération du port. Elle reste le seul point porteur des annotations de cache
 * Spring, qui ne sont honorées que sur le bean proxifié — les déplacer dans un collaborateur les
 * rendrait inopérantes.
 *
 * <ul>
 *   <li>{@link ColecticaCatalogRepository} — listings {@code _query} et remontée des parents</li>
 *   <li>{@link ColecticaPhysicalInstanceReader} / {@link ColecticaPhysicalInstanceWriter} — lecture et
 *       écriture d'une PhysicalInstance</li>
 *   <li>{@link ColecticaSchemeFiler} — rangement des objets sous les schemes de leurs conteneurs</li>
 *   <li>{@link ColecticaGroupSetReader} — lecture d'un Group via {@code ddiset}</li>
 *   <li>{@link ColecticaCodeListRepository} / {@link ColecticaHierarchyBrowser} — listes de codes et
 *       navigation descendante</li>
 *   <li>{@link ColecticaUsageRepository} — « qui utilise cet objet ? »</li>
 *   <li>{@link ColecticaMissingValuesRepository} — valeurs sentinelles</li>
 *   <li>{@link ColecticaItemCreator} — enregistrements unitaires</li>
 * </ul>
 */
public class DDIRepositoryImpl implements DDIRepository {

    private static final Logger logger = LoggerFactory.getLogger(DDIRepositoryImpl.class);

    private final ColecticaCatalogRepository catalog;
    private final ColecticaPhysicalInstanceReader physicalInstanceReader;
    private final ColecticaPhysicalInstanceWriter physicalInstanceWriter;
    private final ColecticaGroupSetReader groupReader;
    private final ColecticaCodeListRepository codeLists;
    private final ColecticaHierarchyBrowser hierarchy;
    private final ColecticaUsageRepository usages;
    private final ColecticaMissingValuesRepository missingValues;
    private final ColecticaItemCreator itemCreator;

    public DDIRepositoryImpl(
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            DDI4toDDI3ConverterService ddi4ToDdi3Converter,
            ColecticaConfiguration colecticaConfiguration,
            ColecticaClient colecticaClient,
            MutualizedCodeListRefsStrategy mutualizedCodeListRefsProvider) {
        String defaultLang = colecticaConfiguration.langs().getFirst();
        ColecticaLabels labels = new ColecticaLabels(defaultLang);
        ColecticaSetReader setReader = new ColecticaSetReader(instanceConfiguration, colecticaClient);
        ColecticaVersionDates versionDates = new ColecticaVersionDates(colecticaClient);

        this.catalog =
                new ColecticaCatalogRepository(instanceConfiguration, colecticaClient, labels, ddi3ToDdi4Converter);
        this.physicalInstanceReader =
                new ColecticaPhysicalInstanceReader(instanceConfiguration, ddi3ToDdi4Converter, setReader);
        this.groupReader = new ColecticaGroupSetReader(colecticaClient, defaultLang);
        this.codeLists = new ColecticaCodeListRepository(
                instanceConfiguration,
                colecticaClient,
                ddi3ToDdi4Converter,
                setReader,
                versionDates,
                labels,
                mutualizedCodeListRefsProvider);
        this.hierarchy = new ColecticaHierarchyBrowser(instanceConfiguration, colecticaClient, catalog, codeLists);
        this.usages = new ColecticaUsageRepository(instanceConfiguration, colecticaClient, labels);
        this.missingValues = new ColecticaMissingValuesRepository(colecticaClient, ddi3ToDdi4Converter, hierarchy);
        ColecticaSchemeFiler schemeFiler = new ColecticaSchemeFiler(
                instanceConfiguration,
                colecticaConfiguration,
                colecticaClient,
                ddi3ToDdi4Converter,
                ddi4ToDdi3Converter,
                mutualizedCodeListRefsProvider,
                catalog,
                defaultLang);
        this.physicalInstanceWriter = new ColecticaPhysicalInstanceWriter(
                instanceConfiguration,
                colecticaClient,
                ddi4ToDdi3Converter,
                physicalInstanceReader,
                schemeFiler,
                labels);
        this.itemCreator = new ColecticaItemCreator(colecticaClient, ddi4ToDdi3Converter);
    }

    // --- Catalogue -------------------------------------------------------------------------------

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        return catalog.getPhysicalInstances();
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstancesViaAdvancedQuery() {
        return catalog.getPhysicalInstancesViaAdvancedQuery();
    }

    /** Mémoïsé, invalidé à chaque écriture de PhysicalInstance. */
    @Override
    @Cacheable(ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS)
    public List<PhysicalInstanceSearchRow> getPhysicalInstanceSearchRows() {
        return catalog.getPhysicalInstanceSearchRows();
    }

    /**
     * Vide la région {@link ColecticaCacheNames#PHYSICAL_INSTANCE_SEARCH_ROWS} pour que le prochain
     * {@link #getPhysicalInstanceSearchRows()} reparcoure Colectica. Les écritures de PhysicalInstance
     * l'évincent déjà ; ceci couvre les changements survenus hors de Bauhaus, qu'aucune écriture locale
     * ne signale.
     */
    @Override
    @CacheEvict(cacheNames = ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS, allEntries = true)
    public void evictPhysicalInstanceSearchRowsCache() {
        logger.info("Physical instance search rows cache evicted");
    }

    @Override
    public List<PartialLogicalProduct> getLogicalProducts() {
        return catalog.getLogicalProducts();
    }

    @Override
    public List<PartialGroup> getGroups() {
        return catalog.getGroups();
    }

    @Override
    public List<PartialStudyUnit> getStudyUnits() {
        return catalog.getStudyUnits();
    }

    @Override
    public List<PartialCodeListScheme> getCodeListSchemes() {
        return catalog.getCodeListSchemes();
    }

    @Override
    public PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id) {
        return catalog.getPhysicalInstanceParents(agencyId, id);
    }

    @Override
    public Optional<String> findStudyUnitXmlByOperationIri(String operationIri) {
        return catalog.findStudyUnitXmlByOperationIri(operationIri);
    }

    @Override
    public Optional<Ddi4StudyUnitResponse> findStudyUnitByOperationIri(String operationIri) {
        return catalog.findStudyUnitByOperationIri(operationIri);
    }

    @Override
    public String getItemXml(String agency, String id, String version) {
        return catalog.getItemXml(agency, id, version);
    }

    @Override
    public String getItemXml(String agency, String id) {
        return catalog.getItemXml(agency, id, null);
    }

    // --- PhysicalInstance ------------------------------------------------------------------------

    @Override
    public Ddi4Response getPhysicalInstance(String agencyId, String id) {
        return physicalInstanceReader.getPhysicalInstance(agencyId, id);
    }

    @Override
    public List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id) {
        return physicalInstanceReader.getPhysicalInstanceCodeLists(agencyId, id);
    }

    @Override
    public Ddi4Response getFullPhysicalInstance(String agencyId, String id) {
        return physicalInstanceReader.getFullPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response getDataRelationships(String agencyId, String id, String version) {
        return physicalInstanceReader.getDataRelationships(agencyId, id, version);
    }

    @Override
    public String getDataRelationshipsXml(String agencyId, String id, String version) {
        return physicalInstanceReader.getDataRelationshipsXml(agencyId, id, version);
    }

    @Override
    @CacheEvict(cacheNames = ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS, allEntries = true)
    public Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request) {
        return physicalInstanceWriter.createPhysicalInstance(request);
    }

    @Override
    @CacheEvict(cacheNames = ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS, allEntries = true)
    public void updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request) {
        physicalInstanceWriter.updatePhysicalInstance(agencyId, id, request);
    }

    @Override
    @CacheEvict(cacheNames = ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS, allEntries = true)
    public void updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response) {
        physicalInstanceWriter.updateFullPhysicalInstance(agencyId, id, ddi4Response);
    }

    // --- Group -----------------------------------------------------------------------------------

    @Override
    public Ddi4GroupResponse getGroup(String agencyId, String id) {
        return groupReader.getGroup(agencyId, id);
    }

    // --- Listes de codes et navigation ------------------------------------------------------------

    @Override
    public Ddi4Response getMutualizedCodesList(String agencyId, String id) {
        return codeLists.getCodeList(agencyId, id, null);
    }

    @Override
    public Ddi4Response getCodeList(String agencyId, String id, String version) {
        return codeLists.getCodeList(agencyId, id, version);
    }

    @Override
    public String getCodeListXml(String agencyId, String id, String version) {
        return codeLists.getCodeListXml(agencyId, id, version);
    }

    /**
     * Mis en cache ({@link ColecticaCacheNames#MUTUALIZED_CODES_LISTS}, TTL configuré par
     * {@code mutualized-codes-cache-ttl}) pour éviter d'interroger Colectica à chaque requête.
     */
    @Override
    @Cacheable(ColecticaCacheNames.MUTUALIZED_CODES_LISTS)
    public List<PartialCodesList> getMutualizedCodesLists() {
        return codeLists.getMutualizedCodesLists();
    }

    /**
     * Vide les deux caches mutualisés pour que le prochain {@link #getMutualizedCodesLists()} reparcoure
     * Colectica : les listes de codes de haut niveau ({@link ColecticaCacheNames#MUTUALIZED_CODES_LISTS})
     * et les références de CodeList du package sous-jacent
     * ({@link ColecticaCacheNames#MUTUALIZED_PACKAGE_CODE_LIST_REFS}, alimentées par
     * {@link MutualizedCodeListRefsProvider}). N'évincer que le premier servirait encore un arbre de
     * package périmé au recalcul : les deux régions sont donc purgées.
     */
    @Override
    @CacheEvict(
            cacheNames = {
                ColecticaCacheNames.MUTUALIZED_CODES_LISTS,
                ColecticaCacheNames.MUTUALIZED_PACKAGE_CODE_LIST_REFS
            },
            allEntries = true)
    public void evictMutualizedCodesListsCache() {
        logger.info("Mutualized codes lists caches evicted");
    }

    @Override
    public List<PartialLogicalProduct> getLogicalProductsByGroup(String agencyId, String groupId) {
        return hierarchy.getLogicalProductsByGroup(agencyId, groupId);
    }

    @Override
    public List<PartialCodeListScheme> getCodeListSchemesByLogicalProduct(String agencyId, String logicalProductId) {
        return hierarchy.getCodeListSchemesByLogicalProduct(agencyId, logicalProductId);
    }

    @Override
    public List<PartialCodesList> getCodeListsByCodeListScheme(String agencyId, String codeListSchemeId) {
        return hierarchy.getCodeListsByCodeListScheme(agencyId, codeListSchemeId);
    }

    @Override
    public List<PartialCodesList> getMissingCodesListsByGroup(String agencyId, String groupId) {
        return hierarchy.getMissingCodesListsByGroup(agencyId, groupId);
    }

    // --- Usages et valeurs sentinelles -------------------------------------------------------------

    @Override
    public List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId) {
        return usages.getVariablesUsingCodeList(codeListAgencyId, codeListId);
    }

    @Override
    public List<CategoryCodeListUsage> getCodeListsUsingCategory(String categoryAgencyId, String categoryId) {
        return usages.getCodeListsUsingCategory(categoryAgencyId, categoryId);
    }

    @Override
    public List<CodeListVariableUsage> getVariablesUsingMissingValuesRepresentation(String agencyId, String mmvrId) {
        return usages.getVariablesUsingMissingValuesRepresentation(agencyId, mmvrId);
    }

    @Override
    public List<PartialMissingValuesRepresentation> getMissingValuesRepresentationsByGroup(
            String agencyId, String groupId) {
        return missingValues.getMissingValuesRepresentationsByGroup(agencyId, groupId);
    }

    // --- Créations unitaires -----------------------------------------------------------------------

    @Override
    public void createLogicalProduct(Ddi4LogicalProduct logicalProduct) {
        itemCreator.createLogicalProduct(logicalProduct);
    }

    @Override
    public void createCodeListScheme(Ddi4CodeListScheme codeListScheme) {
        itemCreator.createCodeListScheme(codeListScheme);
    }

    @Override
    public void createCategoryScheme(Ddi4CategoryScheme categoryScheme) {
        itemCreator.createCategoryScheme(categoryScheme);
    }

    @Override
    public void createVariableScheme(Ddi4VariableScheme variableScheme) {
        itemCreator.createVariableScheme(variableScheme);
    }

    @Override
    public void createManagedRepresentationScheme(Ddi4ManagedRepresentationScheme managedRepresentationScheme) {
        itemCreator.createManagedRepresentationScheme(managedRepresentationScheme);
    }

    @Override
    public void createManagedMissingValuesRepresentation(
            Ddi4ManagedMissingValuesRepresentation managedMissingValuesRepresentation) {
        itemCreator.createManagedMissingValuesRepresentation(managedMissingValuesRepresentation);
    }

    @Override
    public void createCodeList(Ddi4CodeList codeList) {
        itemCreator.createCodeList(codeList);
    }

    @Override
    public void createCategory(Ddi4Category category) {
        itemCreator.createCategory(category);
    }
}
