package fr.insee.rmes.model.operations.documentations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class MSD {

    @JacksonXmlProperty(localName = "mas")
    @JacksonXmlElementWrapper(useWrapping = false)
	private List<MAS> masList;

	public MSD() {
		this.masList = new ArrayList<>();
	}

	public static MSD of(List<MAS> masList) {
		MSD msd = new MSD();
		msd.masList = masList;
		return msd;
	}

	public List<MAS> getMasList() {
		return masList;
	}

	public void setMasList(List<MAS> masList) {
		this.masList = masList;
	}
}
