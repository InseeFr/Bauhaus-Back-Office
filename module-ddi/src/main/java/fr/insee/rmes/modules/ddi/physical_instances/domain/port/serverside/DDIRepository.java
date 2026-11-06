package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CategoryCodeListUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnitResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DuplicatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.MutualizedCodeListCodes;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceIds;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DDIRepository {
    List<PartialPhysicalInstance> getPhysicalInstances();
    /**
     * Same listing as {@link #getPhysicalInstances()} but sourced from Colectica's
     * {@code _query/advanced} endpoint, which returns the per-item property bags (in particular
     * {@code DateProperties.versionDate}). Added alongside the legacy listing for an incremental
     * migration — the two coexist until the advanced query becomes the single source.
     */
    List<PartialPhysicalInstance> getPhysicalInstancesViaAdvancedQuery();
    /**
     * Lignes de recherche avancée : chaque PhysicalInstance jointe à sa StudyUnit et à son Group
     * parents (libellés résolus), via la marche relationnelle {@code byobject}
     * PhysicalInstance ← StudyUnit ← Group. Les parents sont {@code null} quand la relation n'existe pas.
     */
    List<PhysicalInstanceSearchRow> getPhysicalInstanceSearchRows();

    List<PartialLogicalProduct> getLogicalProducts();

    List<PartialGroup> getGroups();

    List<PartialStudyUnit> getStudyUnits();

    /** L'instance physique, ou {@code null} si Colectica ne la connaît pas. */
    Ddi4Response getPhysicalInstance(String agencyId, String id);

    List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id);

    /** Le groupe et ses études, ou {@code null} si Colectica ne connaît pas le groupe. */
    Ddi4GroupResponse getGroup(String agencyId, String id);

    /** Le Group d'identifiant {@code id}, ou {@link Optional#empty()} s'il n'existe pas encore. */
    Optional<Ddi4Group> findGroup(String agencyId, String id);

    /**
     * L'état stocké (dernière version) des items que ce payload réécrit, et d'eux seuls : ce qu'une
     * sauvegarde compare pour réconcilier versions et dates. Les items inconnus du stockage sont absents
     * de la réponse.
     */
    Ddi4Response getStoredItems(Ddi4Response items);

    /**
     * Les mêmes références pointant la dernière version stockée de leur cible, sans en lire le contenu.
     * Les cibles inconnues du stockage sont omises.
     */
    List<Reference> getLatestVersions(List<Reference> references);

    /** La StudyUnit d'identifiant {@code id}, ou {@link Optional#empty()} si elle n'existe pas encore. */
    Optional<Ddi4StudyUnit> findStudyUnit(String agencyId, String id);

    void updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request);

    void updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response);

    /**
     * Copie la PhysicalInstance {@code agencyId/id} (nouveaux identifiants pour la PI, sa
     * DataRelationship, son LogicalRecord et ses variables ; listes de codes et catégories
     * référencées telles quelles) et la rattache à l'Étude de la requête, en un seul enregistrement.
     *
     * @return la référence de la PhysicalInstance créée
     */
    Reference duplicatePhysicalInstance(String agencyId, String id, DuplicatePhysicalInstanceRequest request);

    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request);

    /** Même création, mais avec les identifiants imposés par l'appelant (init local idempotent). */
    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request, PhysicalInstanceIds ids);

    void createLogicalProduct(Ddi4LogicalProduct logicalProduct);

    void createCodeListScheme(Ddi4CodeListScheme codeListScheme);

    void createCategoryScheme(Ddi4CategoryScheme categoryScheme);

    void createVariableScheme(Ddi4VariableScheme variableScheme);

    void createManagedRepresentationScheme(Ddi4ManagedRepresentationScheme managedRepresentationScheme);

    void createManagedMissingValuesRepresentation(
            Ddi4ManagedMissingValuesRepresentation managedMissingValuesRepresentation);

    void createCodeList(Ddi4CodeList codeList);

    void createCategory(Ddi4Category category);

    List<PartialCodesList> getMutualizedCodesLists();

    void evictMutualizedCodesListsCache();

    void evictPhysicalInstanceSearchRowsCache();

    /** Vide toutes les régions de cache Colectica (action d'administration). */
    void evictAllCaches();

    Ddi4Response getMutualizedCodesList(String agencyId, String id);

    /** Valeur et libellé de chaque code d'une liste mutualisée ; {@code null} si elle ne l'est pas. */
    MutualizedCodeListCodes getMutualizedCodeListCodes(String agencyId, String id);

    Ddi4Response getCodeList(String agencyId, String id, String version);

    String getCodeListXml(String agencyId, String id, String version);

    Ddi4Response getDataRelationships(String agencyId, String id, String version);

    String getDataRelationshipsXml(String agencyId, String id, String version);

    List<PartialLogicalProduct> getLogicalProductsByGroup(String agencyId, String groupId);

    List<PartialCodeListScheme> getCodeListSchemes();

    List<PartialCodeListScheme> getCodeListSchemesByLogicalProduct(String agencyId, String logicalProductId);

    List<PartialCodesList> getCodeListsByCodeListScheme(String agencyId, String codeListSchemeId);
    /**
     * Les CodeLists de valeurs sentinelles du groupe (cf. #1566) : celles référencées par une
     * {@code ManagedMissingValuesRepresentation} rangée dans un {@code ManagedRepresentationScheme}
     * des LogicalProducts du groupe.
     */
    List<PartialCodesList> getMissingCodesListsByGroup(String agencyId, String groupId);
    /**
     * Les ManagedMissingValuesRepresentations réutilisables du groupe (cf. #1566) : celles rangées
     * dans un {@code ManagedRepresentationScheme} des LogicalProducts du groupe, avec leur libellé
     * et un aperçu des codes de leur CodeList de sentinelles.
     */
    List<PartialMissingValuesRepresentation> getMissingValuesRepresentationsByGroup(String agencyId, String groupId);

    List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId);
    /**
     * Les CodeLists dont au moins un code référence la catégorie donnée. Alimente la popup de
     * confirmation « catégorie partagée » côté front.
     */
    List<CategoryCodeListUsage> getCodeListsUsingCategory(String categoryAgencyId, String categoryId);
    /**
     * Les variables qui référencent la ManagedMissingValuesRepresentation donnée (cf. #1566), avec
     * leur PhysicalInstance et StudyUnit — même marche {@code byobject} que
     * {@link #getVariablesUsingCodeList}. Alimente la règle lecture seule/écriture des valeurs
     * sentinelles côté front.
     */
    List<CodeListVariableUsage> getVariablesUsingMissingValuesRepresentation(String agencyId, String mmvrId);

    /**
     * Les variables du VariableScheme de la StudyUnit (StudyUnit → LogicalProduct → VariableScheme),
     * chacune dans sa dernière version : le vivier dans lequel une PhysicalInstance de l'étude peut
     * réutiliser une variable (#1387).
     */
    Ddi4Response getStudyUnitVariables(String agencyId, String studyUnitId);

    /**
     * Pour chaque PhysicalInstance de la StudyUnit, une ligne par variable qu'elle utilise : permet de
     * signaler qu'une variable réutilisée est partagée avec d'autres fichiers (#1387).
     */
    List<CodeListVariableUsage> getStudyUnitVariableUsages(String agencyId, String studyUnitId);

    String getItemXml(String agency, String id, String version);

    String getItemXml(String agency, String id);

    PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id);

    /**
     * Les IRIs des séries d'opérations d'un Group (ses {@code r:UserID}), lues sur le seul item du
     * Group — sans télécharger le ddiset de toute sa descendance.
     */
    List<String> getGroupSeriesIris(String agencyId, String groupId);

    /**
     * L'IRI de l'opération dont une StudyUnit est le miroir (son {@code r:UserID}), lue sur le seul
     * item de la StudyUnit ; vide si elle n'en reflète aucune.
     */
    Optional<String> getStudyUnitOperationIri(String agencyId, String studyUnitId);
    /**
     * Le DDI 3.3 de la StudyUnit d'une opération, dans une {@code <FragmentInstance>} qui porte aussi
     * les fragments des PhysicalInstances qu'elle référence (#1145).
     */
    Optional<String> findStudyUnitXmlByOperationIri(String operationIri);

    /** Les mêmes objets — StudyUnit et PhysicalInstances déréférencées — en DDI 4 (#1145). */
    Optional<Ddi4StudyUnitResponse> findStudyUnitByOperationIri(String operationIri);

    /**
     * Les PhysicalInstances de toutes les StudyUnits dont un {@code r:UserID} vaut l'une des
     * {@code operationIris} (une opération peut en avoir plusieurs) ; vide si aucune ne la reflète.
     */
    List<PartialPhysicalInstance> findPhysicalInstancesByOperationIris(Collection<String> operationIris);
}
