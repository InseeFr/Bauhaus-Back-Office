package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;

public interface DDI4toDDI3ConverterService {
    /**
     * Convert DDI4 format to DDI3 format
     * @param ddi4 DDI4 data
     * @return DDI3 formatted data
     */
    Ddi3Response convertDdi4ToDdi3(Ddi4Response ddi4);

    /**
     * Convert DDI4 format to DDI3 XML format
     * @param ddi4 DDI4 data
     * @return DDI3 XML document as String (FragmentInstance format)
     */
    String convertDdi4ToDdi3Xml(Ddi4Response ddi4);

    /**
     * Serialize a single CodeListScheme to a DDI3 item (DDI 3.3 fragment XML + metadata),
     * ready to be sent to Colectica alongside a PhysicalInstance save.
     * @param scheme the code list scheme to serialize
     * @return the DDI3 item for the scheme
     */
    Ddi3Response.Ddi3Item toCodeListSchemeItem(Ddi4CodeListScheme scheme);

    /**
     * Serialize a single CategoryScheme to a DDI3 item (DDI 3.3 fragment XML + metadata),
     * ready to be sent to Colectica.
     * @param scheme the category scheme to serialize
     * @return the DDI3 item for the scheme
     */
    Ddi3Response.Ddi3Item toCategorySchemeItem(Ddi4CategoryScheme scheme);

    /**
     * Serialize a single VariableScheme to a DDI3 item (DDI 3.3 fragment XML + metadata),
     * ready to be sent to Colectica.
     * @param scheme the variable scheme to serialize
     * @return the DDI3 item for the scheme
     */
    Ddi3Response.Ddi3Item toVariableSchemeItem(Ddi4VariableScheme scheme);

    /**
     * Serialize a single ManagedRepresentationScheme to a DDI3 item (DDI 3.3 fragment XML + metadata),
     * ready to be sent to Colectica.
     * @param scheme the managed representation scheme to serialize
     * @return the DDI3 item for the scheme
     */
    Ddi3Response.Ddi3Item toManagedRepresentationSchemeItem(Ddi4ManagedRepresentationScheme scheme);

    /**
     * Serialize a single ManagedMissingValuesRepresentation (valeurs sentinelles, cf. #1566) to a
     * DDI3 item (DDI 3.3 fragment XML + metadata), ready to be sent to Colectica.
     * @param managedMissingValuesRepresentation the managed missing values representation to serialize
     * @return the DDI3 item for the representation
     */
    Ddi3Response.Ddi3Item toManagedMissingValuesRepresentationItem(
            Ddi4ManagedMissingValuesRepresentation managedMissingValuesRepresentation);

    /**
     * Serialize a single CodeList to a DDI3 item (DDI 3.3 fragment XML + metadata),
     * ready to be sent to Colectica.
     * @param codeList the code list to serialize
     * @return the DDI3 item for the code list
     */
    Ddi3Response.Ddi3Item toCodeListItem(Ddi4CodeList codeList);

    /**
     * Serialize a single Category to a DDI3 item (DDI 3.3 fragment XML + metadata),
     * ready to be sent to Colectica.
     * @param category the category to serialize
     * @return the DDI3 item for the category
     */
    Ddi3Response.Ddi3Item toCategoryItem(Ddi4Category category);

    /**
     * Serialize a single LogicalProduct to a DDI3 item (DDI 3.3 fragment XML + metadata),
     * ready to be sent to Colectica.
     * @param logicalProduct the logical product to serialize
     * @return the DDI3 item for the logical product
     */
    Ddi3Response.Ddi3Item toLogicalProductItem(Ddi4LogicalProduct logicalProduct);

    /**
     * Serialize a single Group to a DDI3 item (DDI 3.3 fragment XML + metadata), ready to be sent
     * to Colectica. Used to re-register a group with an added LogicalProductReference when its
     * CodeListScheme is auto-provisioned on save.
     * @param group the group to serialize
     * @param groupItemType the Colectica item type UUID for a Group
     * @return the DDI3 item for the group
     */
    Ddi3Response.Ddi3Item toGroupItem(Ddi4Group group, String groupItemType);

    /**
     * Serialize a single StudyUnit to a DDI3 item (DDI 3.3 fragment XML + metadata), ready to be
     * sent to Colectica. Used to re-register a study unit with an added LogicalProductReference when
     * its VariableScheme is auto-provisioned on save.
     * @param studyUnit the study unit to serialize
     * @param studyUnitItemType the Colectica item type UUID for a StudyUnit
     * @return the DDI3 item for the study unit
     */
    Ddi3Response.Ddi3Item toStudyUnitItem(Ddi4StudyUnit studyUnit, String studyUnitItemType);
}
