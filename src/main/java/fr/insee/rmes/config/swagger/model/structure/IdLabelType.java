package fr.insee.rmes.config.swagger.model.structure;

import io.swagger.v3.oas.annotations.media.Schema;

public class IdLabelType {
	
	@Schema(description = "Id", required = true)
	public String id;
	
	@Schema(description = "Label", required = true)
	public String label;
	
	@Schema(description = "Type", required = true)
	public String type;

}
