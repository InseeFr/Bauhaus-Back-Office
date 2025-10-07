package fr.insee.rmes.model.links;

import java.util.List;

public class Link {
	
	private String typeOfLink;
	private List<String> ids;
	private List<String> urn;
	
	public Link() {
		//nothing to do
	}
	
	public String getTypeOfLink() {
		return typeOfLink;
	}
	public List<String> getIds() {
		return ids;
	}

	public List<String> getUrn() {
		return urn;
	}

}
