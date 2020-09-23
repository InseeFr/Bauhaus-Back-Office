package fr.insee.rmes.model.operations.documentations;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

public class DocumentationRubric {

	private String idAttribute;

	//@JsonProperty( "value" )
	//@JsonFormat(with = {JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
	//@JsonFormat(with = {JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY})
	private List<String> value;

	private String labelLg1;
	private String labelLg2;
	private String codeList;
	private String rangeType;
	private List<Document> documentsLg1;
	private List<Document> documentsLg2;
	
	
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

	
	public boolean isEmpty() {
		return 
		(value == null || value.isEmpty()) &&
		StringUtils.isEmpty(labelLg1) &&
		StringUtils.isEmpty(labelLg2) &&
		StringUtils.isEmpty(codeList) &&
		(documentsLg1 == null || documentsLg1.isEmpty()) &&
		(documentsLg2 == null || documentsLg2.isEmpty());
	}
	
	public boolean hasRichTextLg1() {
		return StringUtils.isNotEmpty(getLabelLg1()) && (getDocumentsLg1() == null || getDocumentsLg1().isEmpty());
	}
	
	public boolean hasRichTextLg2() {
		return StringUtils.isNotEmpty(getLabelLg2()) && (getDocumentsLg2() == null || getDocumentsLg2().isEmpty());
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

	public void setValue(String value) {
		List<String >val = new ArrayList<String>();
		val.add(value);
		this.value = val;
	}
	public List<Document> getDocumentsLg1() {
		return documentsLg1;
	}
	public void setDocumentsLg1(List<Document> documentsLg1) {
		this.documentsLg1 = documentsLg1;
	}
	public List<Document> getDocumentsLg2() {
		return documentsLg2;
	}
	public void setDocumentsLg2(List<Document> documentsLg2) {
		this.documentsLg2 = documentsLg2;
	}

}

