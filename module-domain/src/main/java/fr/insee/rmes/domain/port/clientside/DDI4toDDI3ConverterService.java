package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.model.ddi.Ddi3Response;
import fr.insee.rmes.domain.model.ddi.Ddi4Response;

public interface DDI4toDDI3ConverterService {
    /**
     * Convert DDI4 format to DDI3 format
     * @param ddi4 DDI4 data
     * @return DDI3 formatted data
     */
    Ddi3Response convertDdi4ToDdi3(Ddi4Response ddi4);
}