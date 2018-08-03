package fr.insee.rmes.persistance.service.sesame.links;

import io.swagger.annotations.ApiModelProperty;

public class OperationsLink {

	@ApiModelProperty(value = "Id of the resource linked", required = true)
	public String id;
	
	@ApiModelProperty(value = "Type of object", required = true)
	public String type;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String labelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String labelLg2;

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getLabelLg1() {
		return labelLg1;
	}

	public String getLabelLg2() {
		return labelLg2;
	}

}
