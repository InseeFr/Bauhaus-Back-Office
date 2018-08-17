package fr.insee.rmes.persistance.service.sesame.operations.operations;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import io.swagger.v3.oas.annotations.media.Schema;

public class Operation {

	@Schema(description = "Id", required = true)
	public String id;

	@Schema(description = "Label lg1", required = true)
	public String prefLabelLg1;

	@Schema(description = "Label lg2")
	public String prefLabelLg2;

	@Schema(description = "Alternative label lg1")
	public String altLabelLg1;

	@Schema(description = "Alternative label lg2")
	public String altLabelLg2;

	@Schema(description = "Series")
	public IdLabelTwoLangs series;


	public Operation(String id) {
		this.id=id;
	}


	public String getId() {
		return id;
	}


	public String getPrefLabelLg1() {
		return prefLabelLg1;
	}


	public String getPrefLabelLg2() {
		return prefLabelLg2;
	}


	public String getAltLabelLg1() {
		return altLabelLg1;
	}


	public String getAltLabelLg2() {
		return altLabelLg2;
	}


	public IdLabelTwoLangs getSeries() {
		return series;
	}


}
