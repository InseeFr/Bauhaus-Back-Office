package fr.insee.rmes.persistance.service.sesame.operations.documentations;

import org.apache.commons.lang3.StringUtils;

public class DocumentationRubric {

	public String idAttribute;
	public String value;
	public String labelLg1;
	public String labelLg2;
	public String codeList;
	public String rangeType;
	
	
	public String getIdAttribute() {
		return StringUtils.upperCase(idAttribute);
	}
	public void setIdAttribute(String idAttribute) {
		this.idAttribute = idAttribute;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
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
	public String getCodeList() {
		return codeList;
	}
	public void setCodeList(String codeList) {
		this.codeList = codeList;
	}
	public String getRangeType() {
		return rangeType;
	}
	public void setRangeType(String rangeType) {
		this.rangeType = rangeType;
	}
}

