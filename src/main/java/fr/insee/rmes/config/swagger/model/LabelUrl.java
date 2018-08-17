package fr.insee.rmes.config.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class LabelUrl {
	
	@Schema(description = "Label", required = true)
	public String label;
	
	@Schema(description = "Url", required = true)
	public String url;

}
