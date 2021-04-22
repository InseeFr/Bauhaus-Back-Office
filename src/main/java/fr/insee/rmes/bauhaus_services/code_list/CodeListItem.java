package fr.insee.rmes.bauhaus_services.code_list;

import io.swagger.v3.oas.annotations.media.Schema;

public class CodeListItem {


	@Schema(description = "Code", required = true)
	private String code;
	
	@Schema(description = "Label lg1", required = true)
	private String labelLg1;
	
	@Schema(description = "Label lg2")
	private String labelLg2;

	public CodeListItem(String code, String labelLg1, String labelLg2) {
		super();
		this.code = code;
		this.labelLg1 = labelLg1;
		this.labelLg2 = labelLg2;
	}

	public CodeListItem() {
		super();
	}
	
	public String getCode() {
		return this.code;
	}

	
	public String getLabelLg1() {
		return labelLg1;
	}

	public void setLabelLg1(String labelLg1) {
		this.labelLg1 = labelLg1;
	}

	public String getLabelLg2() {
		return labelLg2;
	}

	public void setLabelLg2(String labelLg2) {
		this.labelLg2 = labelLg2;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static String getClassOperationsLink() {
		return "fr.insee.rmes.bauhaus_services.code_list.CodeListItem";

	}
}