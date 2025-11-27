package fr.insee.rmes.modules.commons.configuration.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class LabelUrl {
	
	@Schema(description = "Label", requiredMode = Schema.RequiredMode.REQUIRED)
	public String label;
	
	@Schema(description = "Url", requiredMode = Schema.RequiredMode.REQUIRED)
	public String url;

}
