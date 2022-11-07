package fr.insee.rmes.model.concepts;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.utils.ExportUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
	private List<MembersLg> membersLg;

	//NOTES
	private String descriptionLg1; 
	private String descriptionLg2;



	public CollectionForExport() {
		membersLg = new ArrayList<>();
	}

	public void addMembers(JSONArray members) {
		for (int i = 0; i < members.length(); i++) {
			JSONObject member = (JSONObject) members.get(i);
			MembersLg rep = new MembersLg();
			rep.setId(member.getString(Constants.ID));
			rep.setPrefLabelLg1(member.getString(Constants.PREF_LABEL_LG1));
			rep.setPrefLabelLg2(member.getString(Constants.PREF_LABEL_LG2));
			if (member.has(Constants.CREATOR)) rep.setCreator(member.getString(Constants.CREATOR));
			if (member.has(Constants.DEF_COURTE_LG1)) rep.setDefCourteLg1(member.getString(Constants.DEF_COURTE_LG1));
			if (member.has(Constants.DEF_COURTE_LG2)) rep.setDefCourteLg2(member.getString(Constants.DEF_COURTE_LG2));
			if (member.has(Constants.DEF_LONGUE_LG1)) rep.setDefLongueLg1(member.getString(Constants.DEF_LONGUE_LG1));
			if (member.has(Constants.DEF_LONGUE_LG2)) rep.setDefLongueLg2(member.getString(Constants.DEF_LONGUE_LG2));
			if (member.has(Constants.EDITORIAL_NOTE_LG1)) rep.setEditorialNoteLg1(member.getString(Constants.EDITORIAL_NOTE_LG1));
			if (member.has(Constants.EDITORIAL_NOTE_LG2)) rep.setEditorialNoteLg2(member.getString(Constants.EDITORIAL_NOTE_LG2));
			if (member.has(Constants.ISVALIDATED)) rep.setIsValidated(ExportUtils.toValidationStatus(member.getString(Constants.ISVALIDATED),true));
			if (member.has(Constants.CREATED)) rep.setCreated(ExportUtils.toDate(member.getString(Constants.CREATED)));
			if (member.has(Constants.MODIFIED)) rep.setModified(ExportUtils.toDate(member.getString(Constants.MODIFIED)));
			membersLg.add(rep);
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



	public List<MembersLg> getMembersLg() {
		return membersLg;
	}



	public void setMembersLg(List<MembersLg> membersLg) {
		this.membersLg = membersLg;
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
