package fr.insee.rmes.config.swagger.model.organizations;

import io.swagger.annotations.ApiModelProperty;

public class Organization {

	@ApiModelProperty(value = "Id", required = true)
	public String id;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String labelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String labelLg2;

	@ApiModelProperty(value = "Alternative label")
	public String altLabel;

	@ApiModelProperty(value = "Uri of Type")
	public String type;

	@ApiModelProperty(value = "Is part of")
	public String motherOrganization;

	@ApiModelProperty(value = "Is linked to")
	public String linkedTo;

	@ApiModelProperty(value = "See also")
	public String seeAlso;
}
