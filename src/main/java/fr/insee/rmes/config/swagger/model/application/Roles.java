package fr.insee.rmes.config.swagger.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

public class Roles {
	
	@Schema(description = "Id", required = true, example = "Id of role")
	public String id;
	
	@Schema(description = "Label", required = true, example = "Role label")
	public String label;
	
	@Schema(description = "Persons", required = true)
	public Persons persons;

}
