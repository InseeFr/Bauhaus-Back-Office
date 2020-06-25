package fr.insee.rmes.model.operations.documentations;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

public class DocumentationRubric {

	private String idAttribute;
	
	@JsonFormat(with = {JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
	private List<String> value;

	private String labelLg1;
	private String labelLg2;
	private String codeList;
	private String rangeType;
	private List<Document> documents;
	
	
	public String getIdAttribute() {
		return StringUtils.upperCase(idAttribute);
	}
	public void setIdAttribute(String idAttribute) {
		this.idAttribute = idAttribute;
	}

	public String getLabelLg1() {
		return labelLg1;
	}
	public void setLabelLg1(String labelLg1) {
		this.labelLg1 = XhtmlToMarkdownUtils.markdownToXhtml(labelLg1);
	}
	public String getLabelLg2() {
		return labelLg2;
	}
	public void setLabelLg2(String labelLg2) {
		this.labelLg2 = XhtmlToMarkdownUtils.markdownToXhtml(labelLg2);
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
	public List<Document> getDocuments() {
		return documents;
	}
	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}
	
	public boolean isEmpty() {
		return 
		(value == null || value.isEmpty()) &&
		StringUtils.isEmpty(labelLg1) &&
		StringUtils.isEmpty(labelLg2) &&
		StringUtils.isEmpty(codeList) &&
		(documents == null || documents.isEmpty());
	}
	public List<String> getValue() {
		return value;
	}
	public void setValue(List<String> value) {
		this.value = value;
	}
	public String getSimpleValue() {
		if (value == null || value.isEmpty()) {
			return null;
		}
		return value.get(0);
	}
}

