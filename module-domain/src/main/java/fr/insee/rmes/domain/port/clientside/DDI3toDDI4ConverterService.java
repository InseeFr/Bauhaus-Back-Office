package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.model.ddi.Ddi3Response;
import fr.insee.rmes.domain.model.ddi.Ddi4Response;

public interface DDI3toDDI4ConverterService {
    /**
     * Convert DDI3 format to DDI4 format
     * @param ddi3 DDI3 data
     * @param schemaUrl URL of the DDI4 JSON schema
     * @return DDI4 formatted data
     */
    Ddi4Response convertDdi3ToDdi4(Ddi3Response ddi3, String schemaUrl);
}