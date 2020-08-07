package fr.insee.rmes.config.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class IdLabelTwoLangs {
	
	@Schema(description = "Id", required = true)
	public String id;
	
	@Schema(description = "Label lg1", required = true)
	public String labelLg1;
	
	@Schema(description = "Label lg2")
	public String labelLg2;

	public IdLabelTwoLangs(String id, String labelLg1, String labelLg2) {
		super();
		this.id = id;
		this.labelLg1 = labelLg1;
		this.labelLg2 = labelLg2;
	}

	public IdLabelTwoLangs() {
		super();
	}
	
	public String getId() {
		return this.id;
	}


}
