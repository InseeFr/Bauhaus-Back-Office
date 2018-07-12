package fr.insee.rmes.config.swagger.model.operations;

import io.swagger.annotations.ApiModelProperty;

public class OperationById {

	@ApiModelProperty(value = "Id", required = true)
	public String id;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String prefLabelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String prefLabelLg2;

	@ApiModelProperty(value = "Alternative label")
	public String altLabel;


	@ApiModelProperty(value = "Uri of Series")
	public String motherSeries;


	@ApiModelProperty(value = "Label series lg1")
	public String motherSeriesLabelLg1;


	@ApiModelProperty(value = "Label series lg2")
	public String motherSeriesLabelLg2;



}
