package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.v3.oas.annotations.media.Schema;

public class CollectionsDashboard {
	
	@Schema(description = "Id", required = true)
	public String id;
	
	@Schema(description = "Label", required = true)
	public String label;
	
	@Schema(description = "Owner", required = true)
	public String creator;
	
	@Schema(description = "Is collection validated", required = true)
	public Boolean isValidated;
	
	@Schema(description = "Creation date", required = true)
	public String created;
	
	@Schema(description = "Modification date")
	public String modified;
	
	@Schema(description = "Number of members")
	public String nbMembers;

}
