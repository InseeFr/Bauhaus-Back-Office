package fr.insee.rmes.config.swagger.model.operations;

import io.swagger.annotations.ApiModelProperty;

public class SeriesLinks {

	@ApiModelProperty(value = "Resource linked", required = true)
	public String uriLinked;

	@ApiModelProperty(value = "Type of link", required = true)
	public String typeOfLink;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String prefLabelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String prefLabelLg2;

}
