package fr.insee.rmes.config.swagger.model.application;

import io.swagger.annotations.ApiModelProperty;

public class Roles {
	
	@ApiModelProperty(value = "Id", required = true, example = "Id of role")
	public String id;
	
	@ApiModelProperty(value = "Label", required = true, example = "Role label")
	public String label;
	
	@ApiModelProperty(value = "Persons", required = true)
	public Persons persons;

}
