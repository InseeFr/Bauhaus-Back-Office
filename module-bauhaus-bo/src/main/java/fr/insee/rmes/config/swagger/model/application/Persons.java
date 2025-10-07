package fr.insee.rmes.config.swagger.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

public class Persons {
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED, example= "Id of Sir Toto")
	public String id;
		
	@Schema(description = "Label", requiredMode = Schema.RequiredMode.REQUIRED, example = "Sir Toto")
	public String label;
	
	@Schema(description = "Stamps", requiredMode = Schema.RequiredMode.REQUIRED, example = "DR59-SIN")
	public String stamp;

}
