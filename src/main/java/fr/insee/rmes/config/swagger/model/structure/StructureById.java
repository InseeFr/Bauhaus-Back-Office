package fr.insee.rmes.config.swagger.model.structure;

import io.swagger.v3.oas.annotations.media.Schema;

public class StructureById {
	
	@Schema(description = "Label lg1", required = true)
	public String labelLg1;
	
	@Schema(description = "Label lg2", required = false)
	public String labelLg2;
	
	@Schema(description = "Description lg1", required = false)
	public String descriptionLg1;
	
	@Schema(description = "Description lg2", required = false)
	public String descriptionLg2;

}
