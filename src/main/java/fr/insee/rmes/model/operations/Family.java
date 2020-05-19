package fr.insee.rmes.model.operations;

import java.util.List;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import io.swagger.v3.oas.annotations.media.Schema;

public class Family {

	@Schema(description = "Id", required = true)
	public String id;

	@Schema(description = "Label lg1", required = true)
	public String prefLabelLg1;

	@Schema(description = "Label lg2")
	public String prefLabelLg2;


	@Schema(description = "Abstract lg1, description")
	public String abstractLg1;


	@Schema(description = "Abstract lg2")
	public String abstractLg2;

	@Schema(description = "Subjects, Topics")
	public List<IdLabelTwoLangs> subjects;

	@Schema(description = "Series")
	public List<IdLabelTwoLangs> series;
	
	
	public Family(String id) {
		this.id=id;
	}

	public String getPrefLabelLg1() {
		return prefLabelLg1;
	}

	public String getId() {
		return id;
	}

	public String getPrefLabelLg2() {
		return prefLabelLg2;
	}

	public String getAbstractLg1() {
		return abstractLg1;
	}

	public String getAbstractLg2() {
		return abstractLg2;
	}



	
}
