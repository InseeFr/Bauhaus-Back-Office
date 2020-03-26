package fr.insee.rmes.modele.concepts;

import java.time.LocalDateTime;
import java.util.List;

public class Collection {
	
	private String id;
	private String prefLabelLg1;
	private String prefLabelLg2;
	private String creator;
	private String contributor;
	private String descriptionLg1;
	private String descriptionLg2;
	private List<String> members;
	private String created;
	private String modified;
	private String isValidated;
	
	public Collection() {
		this.created = LocalDateTime.now().toString();
		this.isValidated = "false";
	}
	
	public Collection(String id) {
		this.id = id;
		this.modified = LocalDateTime.now().toString();
		this.isValidated = "false";
	}
	
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getModified() {
		return modified;
	}
	public void setModified(String modified) {
		this.modified = modified;
	}
	public String getId() {
		return id;
	}
	public String getPrefLabelLg1() {
		return prefLabelLg1;
	}
	public String getPrefLabelLg2() {
		return prefLabelLg2;
	}
	public String getCreator() {
		return creator;
	}
	public String getContributor() {
		return contributor;
	}
	public String getDescriptionLg1() {
		return descriptionLg1;
	}
	public String getDescriptionLg2() {
		return descriptionLg2;
	}
	public List<String> getMembers() {
		return members;
	}

	public Boolean getIsValidated() {
		return Boolean.valueOf(isValidated);
	}
}
