package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;

import java.util.List;
import java.util.Optional;

public interface DDIRepository {
    List<PartialPhysicalInstance> getPhysicalInstances();
    List<PartialLogicalProduct> getLogicalProducts();
    List<PartialGroup> getGroups();
    List<PartialStudyUnit> getStudyUnits();
    Ddi4Response getPhysicalInstance(String agencyId, String id);
    List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id);
    Ddi4GroupResponse getGroup(String agencyId, String id);
    void updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request);
    void updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response);
    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request);
    List<PartialCodesList> getMutualizedCodesLists();
    Ddi4Response getMutualizedCodesList(String agencyId, String id);
    Ddi4Response getCodeList(String agencyId, String id, String version);
    String getCodeListXml(String agencyId, String id, String version);
    Ddi4Response getDataRelationships(String agencyId, String id, String version);
    String getDataRelationshipsXml(String agencyId, String id, String version);
    List<PartialLogicalProduct> getLogicalProductsByGroup(String agencyId, String groupId);
    List<PartialCodeListScheme> getCodeListSchemes();
    List<PartialCodeListScheme> getCodeListSchemesByLogicalProduct(String agencyId, String logicalProductId);
    List<PartialCodesList> getCodeListsByCodeListScheme(String agencyId, String codeListSchemeId);
    List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId);
    String getItemXml(String agency, String id, String version);
    String getItemXml(String agency, String id);
    PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id);
    Optional<String> findStudyUnitXmlByOperationIri(String operationIri);
}