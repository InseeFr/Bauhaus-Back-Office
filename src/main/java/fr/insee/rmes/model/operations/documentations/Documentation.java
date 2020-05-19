package fr.insee.rmes.model.operations.documentations;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Documentation {

	private String id;
	
	private String idOperation;
	private String idSeries;
	private String idIndicator;

	private String labelLg1;
	private String labelLg2;
	private List<DocumentationRubric> rubrics;
	
	
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
	public List<DocumentationRubric> getRubrics() {
		return rubrics;
	}
	public void setRubrics(List<DocumentationRubric> rubrics) {
		this.rubrics = rubrics;
	}
	public String getIdTarget() {
		if (StringUtils.isNotEmpty(idOperation)) {
			return idOperation;
		}
		if (StringUtils.isNotEmpty(idSeries)) {
			return idSeries;
		}
		if (StringUtils.isNotEmpty(idIndicator)) {
			return idIndicator;
		}
		return null;
	}
	public String getIdSeries() {
		return idSeries;
	}
	public void setIdSeries(String idSeries) {
		this.idSeries = idSeries;
	}
	public String getIdIndicator() {
		return idIndicator;
	}
	public void setIdIndicator(String idIndicator) {
		this.idIndicator = idIndicator;
	}
	public String getIdOperation() {
		return idOperation;
	}
}
