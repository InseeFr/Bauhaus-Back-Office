package fr.insee.rmes.config.swagger.model.codeList;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class CodeList {

	@ApiModelProperty(value="Code list's notation")
	public String notation;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String codeListLabelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String codeListLabelLg2;

	@ApiModelProperty(value = "List of codes")
	public List<CodeLabelTwoLangs> codes;



}
