package fr.insee.rmes.modele.links;

import java.util.List;

public class Link {
	
	private String typeOfLink;
	private List<String> ids;
	
	public Link() {
		
	}
	
	public String getTypeOfLink() {
		return typeOfLink;
	}
	public List<String> getIds() {
		return ids;
	}

}
