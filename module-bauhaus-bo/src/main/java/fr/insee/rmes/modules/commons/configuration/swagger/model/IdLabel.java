package fr.insee.rmes.modules.commons.configuration.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class IdLabel {
	
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label", requiredMode = Schema.RequiredMode.REQUIRED)
	public String label;

}
