package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Document {
	
	private String labelLg1;
	private String labelLg2;
	private String descriptionLg1;
	private String descriptionLg2;
	
	
	//private List<StringWithLang> label =  new ArrayList<StringWithLang>();
	//private List<StringWithLang> description =  new ArrayList<StringWithLang>();
	private String dateMiseAJour;
	private String langue;
	private String url;
	private String uri;
	
	/*
	@JsonProperty("labelLg1")
	public List<StringWithLang> getLabel() {
		return label;
	}
	
	public void setLabelLg1(String labelLg1) {
		label.add(new StringWithLang(labelLg1, Lang.FR));
	}
	
	public void setLabelLg2(String labelLg2) {
		if (StringUtils.isNotEmpty(labelLg2)) {
				label.add(new StringWithLang(labelLg2, Lang.EN));
		}
	}
	
	public List<StringWithLang> getDescription() {
		return description;
	}

	public void setDescription(List<StringWithLang> description) {
		this.description = description;
	}
	
	public void setDescriptionLg1(String description) {
		if (StringUtils.isNotEmpty(description)) {
			label.add(new StringWithLang(description, Lang.FR));
		}
	}
	
	public void setDescriptionLg2(String description) {
		if (StringUtils.isNotEmpty(description)) {
			label.add(new StringWithLang(description, Lang.EN));
		}
	}*/
	
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
	public String getDescriptionLg1() {
		return descriptionLg1;
	}
	public void setDescriptionLg1(String descriptionLg1) {
		this.descriptionLg1 = descriptionLg1;
	}
	public String getDescriptionLg2() {
		return descriptionLg2;
	}
	public void setDescriptionLg2(String descriptionLg2) {
		this.descriptionLg2 = descriptionLg2;
	}
	
	@JsonProperty("updatedDate")
	public String getDateMiseAJour() {
		return dateMiseAJour;
	}
	public void setDateMiseAJour(String dateMiseAJour) {
		this.dateMiseAJour = dateMiseAJour;
	}

	@JsonProperty("lang")
	public String getLangue() {
		return langue;
	}
	public void setLangue(String langue) {
		this.langue = langue;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty("uri")
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	
}