package fr.insee.rmes.config.swagger.model.structure;

import io.swagger.v3.oas.annotations.media.Schema;

public class IdLabelType {
	
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label", requiredMode = Schema.RequiredMode.REQUIRED)
	public String label;
	
	@Schema(description = "Type", requiredMode = Schema.RequiredMode.REQUIRED)
	public String type;

}
