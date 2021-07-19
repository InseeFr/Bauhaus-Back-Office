package fr.insee.rmes.model.concepts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.rmes.bauhaus_services.Constants;

public class CollectionForExport {

	//GENERAL
	private String id;//
	private String prefLabelLg1;//
	private String prefLabelLg2;//
	private String creator;//
	private String contributor;//

	//DATE
	private String created;//
	private String modified;//
	
	//STATUS
	private String isValidated;//
	
	//LINKS
	private List<String> membersLg1;
	private List<String> membersLg2;
	
	//NOTES
	private String descriptionLg1; 
	private String descriptionLg2; 	
	
	
	public CollectionForExport() {
		membersLg1 = new ArrayList<>();
		membersLg2 = new ArrayList<>();
	}

	public void addMembers(JSONArray members) {
		for (int i = 0; i < members.length(); i++) {
			JSONObject member = (JSONObject) members.get(i);
				membersLg1.add(member.getString(Constants.PREF_LABEL_LG1));
				membersLg2.add(member.getString(Constants.PREF_LABEL_LG2));
		}
	}

	public String getCreated() {
		return created;
	}

	public String getModified() {
		return modified;
	}

	public String getIsValidated() {
		return isValidated;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public void setPrefLabelLg1(String prefLabelLg1) {
		this.prefLabelLg1 = prefLabelLg1;
	}

	public void setPrefLabelLg2(String prefLabelLg2) {
		this.prefLabelLg2 = prefLabelLg2;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setContributor(String contributor) {
		this.contributor = contributor;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public void setIsValidated(String isValidated) {
		this.isValidated = isValidated;
	}



	public List<String> getMemberLg1() {
		return membersLg1;
	}



	public void setMemberLg1(List<String> memberLg1) {
		this.membersLg1 = memberLg1;
	}



	public List<String> getMemberLg2() {
		return membersLg2;
	}



	public void setMemberLg2(List<String> memberLg2) {
		this.membersLg2 = memberLg2;
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
}
