package fr.insee.rmes.config.swagger.model;

import io.swagger.annotations.ApiModelProperty;

public class IdLabelTwoLangs {
	
	@ApiModelProperty(value = "Id", required = true)
	public String id;
	
	@ApiModelProperty(value = "Label lg1", required = true)
	public String labelLg1;
	
	@ApiModelProperty(value = "Label lg2")
	public String labelLg2;

}
