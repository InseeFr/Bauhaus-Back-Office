package fr.insee.rmes.config.swagger.model.code_list;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeLabelTwoLangs {

	@Schema(description = "Code", required = true)
	public String code;

	@Schema(description = "Label lg1", required = true)
	public String labelLg1;

	@Schema(description = "Label lg2")
	public String labelLg2;

	public String id;
}
