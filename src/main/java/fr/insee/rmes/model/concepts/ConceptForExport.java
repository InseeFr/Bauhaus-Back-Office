package fr.insee.rmes.model.concepts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.rmes.bauhaus_services.Constants;

public class ConceptForExport {

	//GENERAL
	private String id;//
	private String prefLabelLg1;//
	private String prefLabelLg2;//
	private List<String> altLabelLg1;
	private List<String> altLabelLg2;
	private String creator;//
	private String contributor;//
	private String disseminationStatus;//
	
	private Boolean creation;
	private String additionalMaterial;
	private String valid;
	private Boolean versioning;

	//DATE
	private String created;//
	private String modified;//
	
	//STATUS
	private String isValidated;//
	private String conceptVersion;//
	
	//LINKS
	private List<String> narrowerLg1;
	private List<String> narrowerLg2;
	private List<String> broaderLg1;
	private List<String> broaderLg2;
	private List<String> referencesLg1;
	private List<String> referencesLg2;
	private List<String> succeedLg1;
	private List<String> succeedLg2;
	private List<String> relatedLg1;
	private List<String> relatedLg2;
	
	//NOTES
	private String scopeNoteLg1; 
	private String scopeNoteLg2; 
	private String definitionLg1; 
	private String definitionLg2; 
	private String editorialNoteLg1	; 
	private String editorialNoteLg2;
	
	
	
	public ConceptForExport() {
		narrowerLg1 = new ArrayList<>();
		narrowerLg2 = new ArrayList<>();
		broaderLg1= new ArrayList<>();
		broaderLg2 = new ArrayList<>();
		referencesLg1 = new ArrayList<>();
		referencesLg2 = new ArrayList<>();
		succeedLg1 = new ArrayList<>();
		succeedLg2 = new ArrayList<>();
		relatedLg1 = new ArrayList<>();
		relatedLg2 = new ArrayList<>();
	}



	public void addLinks(JSONArray links) {
		for (int i = 0; i < links.length(); i++) {
			JSONObject jsonO = (JSONObject) links.get(i);
			String typeOfLink = jsonO.getString("typeOfLink");
			if (typeOfLink.equals("narrower")) {
				narrowerLg1.add(jsonO.getString(Constants.PREF_LABEL_LG1));
				narrowerLg2.add(jsonO.getString(Constants.PREF_LABEL_LG2));
			}
			else if (typeOfLink.equals("broader")) {
				broaderLg1.add(jsonO.getString(Constants.PREF_LABEL_LG1));
				broaderLg2.add(jsonO.getString(Constants.PREF_LABEL_LG2));
			}
			else if (typeOfLink.equals("references")) {
				referencesLg1.add(jsonO.getString(Constants.PREF_LABEL_LG1));
				referencesLg2.add(jsonO.getString(Constants.PREF_LABEL_LG2));
			}
			else if (typeOfLink.equals("succeed")) {
				succeedLg1.add(jsonO.getString(Constants.PREF_LABEL_LG1));
				succeedLg2.add(jsonO.getString(Constants.PREF_LABEL_LG2));
			}
			else if (typeOfLink.equals("related")) {
				relatedLg1.add(jsonO.getString(Constants.PREF_LABEL_LG1));
				relatedLg2.add(jsonO.getString(Constants.PREF_LABEL_LG2));
			}
		}
		
	}


	public void addNotes(JSONObject notes) {
		if (notes.has("scopeNoteLg1")) {
			scopeNoteLg1 = notes.getString("scopeNoteLg1");
		}
		if (notes.has("scopeNoteLg2")) {
			scopeNoteLg2 = notes.getString("scopeNoteLg2");
		}
		if (notes.has("definitionLg1")) {
			definitionLg1 = notes.getString("definitionLg1");
		}
		if (notes.has("definitionLg2")) {
			definitionLg2 = notes.getString("definitionLg2");
		}
		if (notes.has("editorialNoteLg1")) {
			editorialNoteLg1 = notes.getString("editorialNoteLg1");
		}
		if (notes.has("editorialNoteLg2")) {
			editorialNoteLg2 = notes.getString("editorialNoteLg2");
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

	public List<String> getAltLabelLg1() {
		return altLabelLg1;
	}

	public List<String> getAltLabelLg2() {
		return altLabelLg2;
	}

	public String getCreator() {
		return creator;
	}

	public String getContributor() {
		return contributor;
	}

	public String getDisseminationStatus() {
		return disseminationStatus;
	}

	public String getAdditionalMaterial() {
		return additionalMaterial;
	}

	public String getValid() {
		return valid;
	}

	public Boolean getCreation() {
		return creation;
	}
	
	public Boolean getVersioning() {
		return versioning;
	}

	public String getConceptVersion() {
		return conceptVersion;
	}

	public void setConceptVersion(String conceptVersion) {
		this.conceptVersion = conceptVersion;
	}

	public List<String> getNarrowerLg1() {
		return narrowerLg1;
	}

	public void setNarrowerLg1(List<String> narrowerLg1) {
		this.narrowerLg1 = narrowerLg1;
	}

	public List<String> getNarrowerLg2() {
		return narrowerLg2;
	}

	public void setNarrowerLg2(List<String> narrowerLg2) {
		this.narrowerLg2 = narrowerLg2;
	}

	public List<String> getBroaderLg1() {
		return broaderLg1;
	}

	public void setBroaderLg1(List<String> broaderLg1) {
		this.broaderLg1 = broaderLg1;
	}

	public List<String> getBroaderLg2() {
		return broaderLg2;
	}

	public void setBroaderLg2(List<String> broaderLg2) {
		this.broaderLg2 = broaderLg2;
	}

	public List<String> getReferencesLg1() {
		return referencesLg1;
	}

	public void setReferencesLg1(List<String> referencesLg1) {
		this.referencesLg1 = referencesLg1;
	}

	public List<String> getReferencesLg2() {
		return referencesLg2;
	}

	public void setReferencesLg2(List<String> referencesLg2) {
		this.referencesLg2 = referencesLg2;
	}

	public List<String> getSucceedLg1() {
		return succeedLg1;
	}

	public void setSucceedLg1(List<String> succeedLg1) {
		this.succeedLg1 = succeedLg1;
	}

	public List<String> getSucceedLg2() {
		return succeedLg2;
	}

	public void setSucceedLg2(List<String> succeedLg2) {
		this.succeedLg2 = succeedLg2;
	}

	public List<String> getRelatedLg1() {
		return relatedLg1;
	}

	public void setRelatedLg1(List<String> relatedLg1) {
		this.relatedLg1 = relatedLg1;
	}

	public List<String> getRelatedLg2() {
		return relatedLg2;
	}

	public void setRelatedLg2(List<String> relatedLg2) {
		this.relatedLg2 = relatedLg2;
	}

	public void setPrefLabelLg1(String prefLabelLg1) {
		this.prefLabelLg1 = prefLabelLg1;
	}

	public void setPrefLabelLg2(String prefLabelLg2) {
		this.prefLabelLg2 = prefLabelLg2;
	}

	public void setAltLabelLg1(List<String> altLabelLg1) {
		this.altLabelLg1 = altLabelLg1;
	}

	public void setAltLabelLg2(List<String> altLabelLg2) {
		this.altLabelLg2 = altLabelLg2;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setContributor(String contributor) {
		this.contributor = contributor;
	}

	public void setDisseminationStatus(String disseminationStatus) {
		this.disseminationStatus = disseminationStatus;
	}

	public void setAdditionalMaterial(String additionalMaterial) {
		this.additionalMaterial = additionalMaterial;
	}

	public void setValid(String valid) {
		this.valid = valid;
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

	public void setCreation(Boolean creation) {
		this.creation = creation;
	}

	public void setVersioning(Boolean versioning) {
		this.versioning = versioning;
	}

	public String getScopeNoteLg1() {
		return scopeNoteLg1;
	}

	public void setScopeNoteLg1(String scopeNoteLg1) {
		this.scopeNoteLg1 = scopeNoteLg1;
	}

	public String getScopeNoteLg2() {
		return scopeNoteLg2;
	}

	public void setScopeNoteLg2(String scopeNoteLg2) {
		this.scopeNoteLg2 = scopeNoteLg2;
	}

	public String getDefinitionLg1() {
		return definitionLg1;
	}

	public void setDefinitionLg1(String definitionLg1) {
		this.definitionLg1 = definitionLg1;
	}

	public String getDefinitionLg2() {
		return definitionLg2;
	}

	public void setDefinitionLg2(String definitionLg2) {
		this.definitionLg2 = definitionLg2;
	}

	public String getEditorialNoteLg1() {
		return editorialNoteLg1;
	}

	public void setEditorialNoteLg1(String editorialNoteLg1) {
		this.editorialNoteLg1 = editorialNoteLg1;
	}

	public String getEditorialNoteLg2() {
		return editorialNoteLg2;
	}

	public void setEditorialNoteLg2(String editorialNoteLg2) {
		this.editorialNoteLg2 = editorialNoteLg2;
	}


}
