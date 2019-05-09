package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

import org.openrdf.model.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

public class Document {
	
	private String labelLg1;
	private String labelLg2;
	private String descriptionLg1;
	private String descriptionLg2;
	private String dateMiseAJour;
	private String langue;
	private String url;
	private URI uri;
	

	public Document(String id) {
		this.uri = SesameUtils.documentIRI(id);
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
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}
	public void setUri(String id) {
		this.uri =  SesameUtils.documentIRI(id);
	}
	
}