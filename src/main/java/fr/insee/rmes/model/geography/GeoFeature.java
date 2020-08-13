package fr.insee.rmes.model.geography;

import java.util.List;

public class GeoFeature {
	
	private String id;
	private String labelLg1;
	private String labelLg2;
	private List<GeoFeature> unions;
	private GeoFeature difference;
	private String code;
	private String uri;
	private String descriptionLg1;
	private String descriptionLg2;
	private String typeTerritory;
	
	
	public GeoFeature() {
		super();
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
	public List<GeoFeature> getUnions() {
		return unions;
	}
	public void setUnions(List<GeoFeature> unions) {
		this.unions = unions;
	}
	public GeoFeature getDifference() {
		return difference;
	}
	public void setDifference(GeoFeature difference) {
		this.difference = difference;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
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
	public String getTypeTerritory() {
		return typeTerritory;
	}
	public void setTypeTerritory(String typeTerritory) {
		this.typeTerritory = typeTerritory;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	

}
