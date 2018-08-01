package fr.insee.rmes.config.swagger.model.operations;

import io.swagger.annotations.ApiModelProperty;

public class Links {

	@ApiModelProperty(value = "Id of the resource linked", required = true)
	public String id;
	
	@ApiModelProperty(value = "Type of object", required = true)
	public String type;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String labelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String labelLg2;

}
