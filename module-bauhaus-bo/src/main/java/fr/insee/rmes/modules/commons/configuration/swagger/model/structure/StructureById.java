package fr.insee.rmes.modules.commons.configuration.swagger.model.structure;

import io.swagger.v3.oas.annotations.media.Schema;

public class StructureById {
	
	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String labelLg1;
	
	@Schema(description = "Label lg2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	public String labelLg2;
	
	@Schema(description = "Description lg1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	public String descriptionLg1;
	
	@Schema(description = "Description lg2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	public String descriptionLg2;

}
