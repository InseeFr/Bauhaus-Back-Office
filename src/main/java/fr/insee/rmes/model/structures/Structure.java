package fr.insee.rmes.model.structures;

import java.util.List;

import fr.insee.rmes.exceptions.RmesException;

public class Structure {

	private String id;
	private String labelLg1;
	private String labelLg2;
	private String descriptionLg1;
	private String descriptionLg2;
	private List<ComponentDefinition> componentDefinitions;
	private String created;



	private String updated;

	public Structure() throws RmesException {

	}

	public Structure(String id) throws RmesException {
		this.id = id;
	}

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

	public List<ComponentDefinition> getComponentDefinitions() {
		return componentDefinitions;
	}

	public void setComponentDefinitions(List<ComponentDefinition> componentDefinitions) {
		this.componentDefinitions = componentDefinitions;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

}
