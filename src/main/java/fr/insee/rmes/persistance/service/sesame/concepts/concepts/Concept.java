package fr.insee.rmes.persistance.service.sesame.concepts.concepts;

import java.time.LocalDateTime;
import java.util.List;

import fr.insee.rmes.persistance.service.sesame.links.Link;
import fr.insee.rmes.persistance.service.sesame.notes.DatableNote;
import fr.insee.rmes.persistance.service.sesame.notes.VersionableNote;

public class Concept {

	private String id;
	private String prefLabelLg1;
	private String prefLabelLg2;
	private List<String> altLabelLg1;
	private List<String> altLabelLg2;
	private String creator;
	private String contributor;
	private String disseminationStatus;
	private String additionalMaterial;
	private String valid;
	private List<VersionableNote> versionableNotes;
	private List<DatableNote> datableNotes;
	private List<Link> links;
	private String created;
	private String modified;
	private String isValidated;
	private Boolean creation;
	private Boolean versioning;

	
	public Concept() {
		this.id = new ConceptsUtils().createID();
		this.created = LocalDateTime.now().toString();
		this.isValidated = "Provisoire";
		this.creation = true;
		this.versioning = false;
	}
	
	public Concept(String id) {
		this.id = id;
		this.modified = LocalDateTime.now().toString();
		this.isValidated = "Provisoire";
		this.creation = false;
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

	public List<VersionableNote> getVersionableNotes() {
		return versionableNotes;
	}
	
	public List<DatableNote> getDatableNotes() {
		return datableNotes;
	}
	
	public List<Link> getLinks() {
		return links;
	}
	
	public Boolean getCreation() {
		return creation;
	}
	
	public Boolean getVersioning() {
		return versioning;
	}
}
