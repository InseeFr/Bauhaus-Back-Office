package fr.insee.rmes.config.swagger.model.codeList;

import io.swagger.annotations.ApiModelProperty;

public class CodeLabelTwoLangs {

	@ApiModelProperty(value = "Code", required = true)
	public String code;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String labelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String labelLg2;

}
