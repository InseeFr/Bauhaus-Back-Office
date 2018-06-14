package fr.insee.rmes.config.swagger.model.operations;

import io.swagger.annotations.ApiModelProperty;

public class SeriesById {

	@ApiModelProperty(value = "Id", required = true)
	public String id;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String prefLabelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String prefLabelLg2;

	@ApiModelProperty(value = "Alternative label lg1")
	public String altLabelLg1;

	@ApiModelProperty(value = "Alternative label lg2")
	public String altLabelLg2;

	// TODO complete properties...
}
