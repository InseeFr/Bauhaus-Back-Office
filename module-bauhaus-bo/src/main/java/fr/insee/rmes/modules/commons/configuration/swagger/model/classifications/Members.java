package fr.insee.rmes.modules.commons.configuration.swagger.model.classifications;

import io.swagger.v3.oas.annotations.media.Schema;

public class Members {
	
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String labelLg1;
	
	@Schema(description = "Label lg2")
	public String labelLg2;

}
