package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import com.fasterxml.jackson.databind.JsonNode;

public interface DDIItemConvertService {
    JsonNode convert(String xmlFragment);
}
