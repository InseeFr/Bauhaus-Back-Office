package fr.insee.rmes.modules.operations.families.webservice.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import org.springframework.boot.jackson.JacksonMixin;

@JacksonMixin(OperationFamily.class)
@JsonIgnoreProperties(value = {"series", "subjects"})
public class OperationFamilyMixin {}
