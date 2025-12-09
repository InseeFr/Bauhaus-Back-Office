package fr.insee.rmes.modules.commons.configuration.swagger.model.code_list;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeLabelTwoLangs {

	public String code;

	public String labelLg1;

	public String labelLg2;

	public String id;
}
