package fr.insee.rmes.model.operations.documentations;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class MSD {

    @JacksonXmlProperty(localName = "mas")
    @JacksonXmlElementWrapper(useWrapping = false)
	private List<MAS> masList;

	public MSD() {
		this.masList = new ArrayList<MAS>();
	}
	
	public MSD(List<MAS> masList) {
		this.masList = masList;
	}

	public List<MAS> getMasList() {
		return masList;
	}

	public void setMasList(List<MAS> masList) {
		this.masList = masList;
	}
}
