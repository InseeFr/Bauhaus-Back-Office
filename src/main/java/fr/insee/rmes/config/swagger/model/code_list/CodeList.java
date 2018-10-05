package fr.insee.rmes.config.swagger.model.code_list;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class CodeList {

	@Schema(description="Code list's notation")
	public String notation;

	@Schema(description = "Label lg1", required = true)
	public String codeListLabelLg1;

	@Schema(description = "Label lg2")
	public String codeListLabelLg2;

	@Schema(description = "List of codes")
	public List<CodeLabelTwoLangs> codes;



}
