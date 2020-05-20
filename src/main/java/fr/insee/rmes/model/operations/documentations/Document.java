package fr.insee.rmes.model.operations.documentations;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.insee.rmes.bauhaus_services.rdfUtils.RdfUtils;
import fr.insee.rmes.utils.DateParser;

public class Document {
	
	private String labelLg1;
	private String labelLg2;
	private String descriptionLg1;
	private String descriptionLg2;
	private String dateMiseAJour;
	private String langue;
	private String url;
	private String uri;

	public Document() {
	}
	
	public Document(String id) {
		this.uri = RdfUtils.documentIRI(id).toString();
	}

	public Document(String id, boolean isLink) {
		if (isLink) {this.uri = RdfUtils.linkIRI(id).toString();}
		else {this.uri = RdfUtils.documentIRI(id).toString();}
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
		return DateParser.getDate(dateMiseAJour);
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

	public String getUri() {
		return uri;
	}
		
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getId() {
		return StringUtils.substringAfter(uri, "/");
	}
}