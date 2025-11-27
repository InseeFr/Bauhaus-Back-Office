package fr.insee.rmes.modules.commons.configuration.swagger.model.code_list;

import io.swagger.v3.oas.annotations.media.Schema;

public class CodeLabelList {
	@Schema(description = "Code", requiredMode = Schema.RequiredMode.REQUIRED)
	public String code;

	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String labelLg1;

	@Schema(description = "Label lg2")
	public String labelLg2;

	public String notationCodeList;
}
