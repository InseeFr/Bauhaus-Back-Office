package fr.insee.rmes.model.operations.documentations;

import java.util.ArrayList;
import java.util.List;

public class MSD {
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
