package fr.insee.rmes.modules.commons.configuration.swagger.model.concepts;

import io.swagger.v3.oas.annotations.media.Schema;

public class ConceptsToValidate {

	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label", requiredMode = Schema.RequiredMode.REQUIRED)
	public String label;
	
	@Schema(description = "Owner", requiredMode = Schema.RequiredMode.REQUIRED)
	public String creator;
	
	@Schema(description = "Expiration date")
	public String valid;
	
}
