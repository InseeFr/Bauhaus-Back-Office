package fr.insee.rmes.bauhaus_services.code_list;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailedCodeList {


	public String notation;
	
	public String codeListLabelLg1;
	
	public String codeListLabelLg2;
	
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
