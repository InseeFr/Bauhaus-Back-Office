package fr.insee.rmes.persistance.service.sesame.dsd;

import fr.insee.rmes.exceptions.RmesException;

public class Component {
	
	private String id;
	private String labelLg1;
	private String labelLg2;
	private String type;
	private String concept;
	private String codeList;
	private String range;
	private String attachment;
	
	public Component() throws RmesException {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getCodeList() {
		return codeList;
	}

	public void setCodeList(String codeList) {
		this.codeList = codeList;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}
	
	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

}
