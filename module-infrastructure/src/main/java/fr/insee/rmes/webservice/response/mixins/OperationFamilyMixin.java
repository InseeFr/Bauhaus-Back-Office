package fr.insee.rmes.webservice.response.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"series", "subjects"})
public class OperationFamilyMixin {
}
