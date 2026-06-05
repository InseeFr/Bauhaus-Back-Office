package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;

public interface DDI3toDDI4ConverterService {
    /**
     * Convert DDI3 format to DDI4 format
     * @param ddi3 DDI3 data
     * @param schemaUrl URL of the DDI4 JSON schema
     * @return DDI4 formatted data
     */
    Ddi4Response convertDdi3ToDdi4(Ddi3Response ddi3, String schemaUrl);

    /**
     * Parse a single CodeListScheme from its DDI 3.3 fragment XML (as returned by Colectica
     * {@code GET item}). Used to read the existing scheme of a group before merging new
     * code list references into it.
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a CodeListScheme
     * @return the parsed code list scheme
     */
    Ddi4CodeListScheme toCodeListScheme(String fragmentXml);
}