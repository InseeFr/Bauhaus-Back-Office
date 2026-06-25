package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;

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
     * Serialize a single LogicalProduct to a DDI3 item (DDI 3.3 fragment XML + metadata),
     * ready to be sent to Colectica.
     * @param logicalProduct the logical product to serialize
     * @return the DDI3 item for the logical product
     */
    Ddi3Response.Ddi3Item toLogicalProductItem(Ddi4LogicalProduct logicalProduct);
}