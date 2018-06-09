package fr.insee.rmes.config.swagger.model.application;

import io.swagger.annotations.ApiModelProperty;

public class Persons {
	
	@ApiModelProperty(value = "Id", required = true, example = "Id of Sir Toto")
	public String id;
	
	@ApiModelProperty(value = "Label", required = true, example = "Sir Toto")
	public String label;
	
	@ApiModelProperty(value = "Stamps", required = true, example = "DR59-SIN")
	public String stamp;

}
