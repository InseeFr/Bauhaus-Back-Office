package fr.insee.rmes.persistance.service.sesame.operations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleObjectForList {
	
	@JsonProperty("id")
	public String id;
	
	@JsonProperty("label")
	public String label;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String toString(){
    	return this.id ;
    }

}
