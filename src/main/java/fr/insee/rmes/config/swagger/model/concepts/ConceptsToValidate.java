package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.annotations.ApiModelProperty;

public class ConceptsToValidate {

	@ApiModelProperty(value = "Id", required = true)
	public String id;
	
	@ApiModelProperty(value = "Label", required = true)
	public String label;
	
	@ApiModelProperty(value = "Owner", required = true)
	public String creator;
	
	@ApiModelProperty(value = "Expiration date")
	public String valid;
	
}
