package fr.insee.rmes.config.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class IdLabelTwoLangs {
	
	@Schema(description = "Id", required = true)
	public String id;
	
	@Schema(description = "Label lg1", required = true)
	public String labelLg1;
	
	@Schema(description = "Label lg2")
	public String labelLg2;

}
