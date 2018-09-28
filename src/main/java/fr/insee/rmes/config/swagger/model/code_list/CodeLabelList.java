package fr.insee.rmes.config.swagger.model.code_list;

import io.swagger.v3.oas.annotations.media.Schema;

public class CodeLabelList {
	@Schema(description = "Code", required = true)
	public String code;

	@Schema(description = "Label lg1", required = true)
	public String labelLg1;

	@Schema(description = "Label lg2")
	public String labelLg2;

	@Schema(description="Code list's notation")
	public String notationCodeList;

}
