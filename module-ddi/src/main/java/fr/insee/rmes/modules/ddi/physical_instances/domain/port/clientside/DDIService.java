package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;


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
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DDIService {
    List<PartialPhysicalInstance> getPhysicalInstances();
    List<PartialPhysicalInstance> getPhysicalInstancesFilteredByStamp(Set<String> userStamps);
    List<PartialLogicalProduct> getLogicalProducts();
    List<PartialGroup> getGroups();
    List<PartialGroup> getGroupsFilteredByStamp(Set<String> userStamps);
    Ddi4Response getDdi4PhysicalInstance(String agencyId, String id);
    List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id);
    Ddi4GroupResponse getDdi4Group(String agencyId, String id);
    Ddi4Response updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request);
    Ddi4Response updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response);
    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request);
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
    List<PartialCodesList> getCodeListsByGroup(String agencyId, String groupId);
    List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId);
    String getItemXml(String agency, String id, String version);
    String getItemXml(String agency, String id);
    PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id);
    Optional<String> getStudyUnitXmlByOperationIri(String operationIri);
}