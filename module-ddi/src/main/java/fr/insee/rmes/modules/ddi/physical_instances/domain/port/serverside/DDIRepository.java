package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;

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
    Ddi4Response getPhysicalInstance(String agencyId, String id);
    List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id);
    Ddi4GroupResponse getGroup(String agencyId, String id);
    void updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request);
    void updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response);
    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request);
    void createLogicalProduct(Ddi4LogicalProduct logicalProduct);
    void createCodeListScheme(Ddi4CodeListScheme codeListScheme);
    void createCategoryScheme(Ddi4CategoryScheme categoryScheme);
    void createVariableScheme(Ddi4VariableScheme variableScheme);
    void createManagedRepresentationScheme(Ddi4ManagedRepresentationScheme managedRepresentationScheme);
    void createManagedMissingValuesRepresentation(Ddi4ManagedMissingValuesRepresentation managedMissingValuesRepresentation);
    void createCodeList(Ddi4CodeList codeList);
    void createCategory(Ddi4Category category);
    List<PartialCodesList> getMutualizedCodesLists();
    void evictMutualizedCodesListsCache();
    Ddi4Response getMutualizedCodesList(String agencyId, String id);
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
     * Les variables qui référencent la ManagedMissingValuesRepresentation donnée (cf. #1566), avec
     * leur PhysicalInstance et StudyUnit — même marche {@code byobject} que
     * {@link #getVariablesUsingCodeList}. Alimente la règle lecture seule/écriture des valeurs
     * sentinelles côté front.
     */
    List<CodeListVariableUsage> getVariablesUsingMissingValuesRepresentation(String agencyId, String mmvrId);
    /**
     * Supprime une ManagedMissingValuesRepresentation sans usage (cf. #1566) : défile ses
     * références des schemes du groupe puis supprime la MMVR, sa CodeList de sentinelles et les
     * catégories de celle-ci. Refuse (exception typée) si au moins une variable la référence.
     */
    void deleteMissingValuesRepresentation(String agencyId, String mmvrId);
    String getItemXml(String agency, String id, String version);
    String getItemXml(String agency, String id);
    PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id);
    Optional<String> findStudyUnitXmlByOperationIri(String operationIri);
}