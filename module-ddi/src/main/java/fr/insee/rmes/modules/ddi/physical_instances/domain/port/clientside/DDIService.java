package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CategoryCodeListUsage;
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
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnitResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DDIService {
    List<PartialPhysicalInstance> getPhysicalInstances();
    List<PartialPhysicalInstance> getPhysicalInstancesFilteredByStamp(Set<String> userStamps);
    List<PhysicalInstanceSearchRow> searchPhysicalInstances();
    List<PhysicalInstanceSearchRow> searchPhysicalInstancesFilteredByStamp(Set<String> userStamps);
    List<PartialLogicalProduct> getLogicalProducts();
    List<PartialGroup> getGroups();
    List<PartialGroup> getGroupsFilteredByStamp(Set<String> userStamps);
    Ddi4Response getDdi4PhysicalInstance(String agencyId, String id);
    List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id);
    Ddi4GroupResponse getDdi4Group(String agencyId, String id);
    Ddi4Response updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request);
    Ddi4Response updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response);
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
    void evictPhysicalInstanceSearchRowsCache();
    Ddi4Response getMutualizedCodesList(String agencyId, String id);
    Ddi4Response getCodeList(String agencyId, String id, String version);
    String getCodeListXml(String agencyId, String id, String version);
    Ddi4Response getDataRelationships(String agencyId, String id, String version);
    String getDataRelationshipsXml(String agencyId, String id, String version);
    List<PartialLogicalProduct> getLogicalProductsByGroup(String agencyId, String groupId);
    List<PartialCodeListScheme> getCodeListSchemes();
    List<PartialCodeListScheme> getCodeListSchemesByLogicalProduct(String agencyId, String logicalProductId);
    List<PartialCodesList> getCodeListsByCodeListScheme(String agencyId, String codeListSchemeId);
    List<PartialCodesList> getCodeListsByGroup(String agencyId, String groupId);
    /**
     * Les CodeLists de valeurs sentinelles du groupe (cf. #1566) : celles référencées par une
     * {@code ManagedMissingValuesRepresentation} d'un {@code ManagedRepresentationScheme} du groupe.
     */
    List<PartialCodesList> getMissingCodesListsByGroup(String agencyId, String groupId);
    /**
     * Les ManagedMissingValuesRepresentations réutilisables du groupe (cf. #1566), avec libellé et
     * aperçu des codes de leur CodeList de sentinelles — alimente le sélecteur de réutilisation.
     */
    List<PartialMissingValuesRepresentation> getMissingValuesRepresentationsByGroup(String agencyId, String groupId);
    List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId);
    /**
     * Les CodeLists dont au moins un code référence la catégorie donnée — alimente la popup de
     * confirmation « catégorie partagée » côté front.
     */
    List<CategoryCodeListUsage> getCodeListsUsingCategory(String categoryAgencyId, String categoryId);
    /**
     * Les variables qui référencent la ManagedMissingValuesRepresentation donnée (cf. #1566) —
     * alimente la règle lecture seule/écriture des valeurs sentinelles côté front.
     */
    List<CodeListVariableUsage> getVariablesUsingMissingValuesRepresentation(String agencyId, String mmvrId);
    String getItemXml(String agency, String id, String version);
    String getItemXml(String agency, String id);
    PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id);
    Optional<String> getStudyUnitXmlByOperationIri(String operationIri);

    /**
     * La StudyUnit d'une opération et les PhysicalInstances qu'elle référence, en DDI 4 (#1145) —
     * pendant JSON de {@link #getStudyUnitXmlByOperationIri(String)}.
     */
    Optional<Ddi4StudyUnitResponse> getStudyUnitByOperationIri(String operationIri);
}