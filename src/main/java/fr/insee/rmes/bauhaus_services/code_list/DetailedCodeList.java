package fr.insee.rmes.bauhaus_services.code_list;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailedCodeList {


	@Schema(description = "Notation", required = true)
	public String notation;
	
	@Schema(description = "Label lg1", required = true)
	public String codeListLabelLg1;
	
	@Schema(description = "Label lg2")
	public String codeListLabelLg2;
	
	@Schema(description = "Codes")
	public List<CodeListItem> codes;

	public String getNotation() {
		return notation;
	}

	public void setNotation(String notation) {
		this.notation = notation;
	}

	public String getCodeListLabelLg1() {
		return codeListLabelLg1;
	}

	public void setCodeListLabelLg1(String codeListLabelLg1) {
		this.codeListLabelLg1 = codeListLabelLg1;
	}

	public String getCodeListLabelLg2() {
		return codeListLabelLg2;
	}

	public void setCodeListLabelLg2(String codeListLabelLg2) {
		this.codeListLabelLg2 = codeListLabelLg2;
	}

	public List<CodeListItem> getCodes() {
		return codes;
	}

	public void setCodes(List<CodeListItem> codes) {
		this.codes = codes;
	}
	
}
