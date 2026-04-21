package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import com.fasterxml.jackson.databind.JsonNode;

public interface DDIItemConverter {
    boolean supports(String xmlRootElementLocalName);
    JsonNode convert(String xmlFragment);
}
