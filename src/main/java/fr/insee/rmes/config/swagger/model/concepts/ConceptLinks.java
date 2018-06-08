package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.annotations.ApiModelProperty;

public class ConceptLinks {

	@ApiModelProperty(value = "Id", required = true)
	public String id;
	
	@ApiModelProperty(value = "Type of link", required = true)
	public String typeOfLink;
	
	@ApiModelProperty(value = "Label lg1", required = true)
	public String prefLabelLg1;
	
	@ApiModelProperty(value = "Label lg2")
	public String prefLabelLg2;

}
