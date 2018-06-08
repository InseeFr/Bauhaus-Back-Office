package fr.insee.rmes.config.swagger.model;

import io.swagger.annotations.ApiModelProperty;

public class IdLabelAltLabel {

	@ApiModelProperty(value = "Id", required = true)
	public String id;
	
	@ApiModelProperty(value = "Label", required = true)
	public String label;
	
	@ApiModelProperty(value = "Alternative label")
	public String altLabel;
	
}
