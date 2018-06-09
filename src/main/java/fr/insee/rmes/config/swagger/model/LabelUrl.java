package fr.insee.rmes.config.swagger.model;

import io.swagger.annotations.ApiModelProperty;

public class LabelUrl {
	
	@ApiModelProperty(value = "Label", required = true)
	public String label;
	
	@ApiModelProperty(value = "Url", required = true)
	public String url;

}
