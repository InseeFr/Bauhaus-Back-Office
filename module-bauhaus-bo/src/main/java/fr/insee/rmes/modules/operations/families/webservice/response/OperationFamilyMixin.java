package fr.insee.rmes.modules.operations.families.webservice.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"series", "subjects"})
public class OperationFamilyMixin {
}
