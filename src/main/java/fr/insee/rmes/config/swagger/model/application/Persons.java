package fr.insee.rmes.config.swagger.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

public class Persons {
	@Schema(description = "Id", required=true, example= "Id of Sir Toto")
	public String id;
		
	@Schema(description = "Label", required = true, example = "Sir Toto")
	public String label;
	
	@Schema(description = "Stamps", required = true, example = "DR59-SIN")
	public String stamp;

}
