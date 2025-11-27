package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
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
}