package fr.insee.rmes.modules.commons.configuration.swagger.model.concepts;

import io.swagger.v3.oas.annotations.media.Schema;

public class CollectionsDashboard {
	
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label", requiredMode = Schema.RequiredMode.REQUIRED)
	public String label;
	
	@Schema(description = "Owner", requiredMode = Schema.RequiredMode.REQUIRED)
	public String creator;
	
	@Schema(description = "Is collection validated", requiredMode = Schema.RequiredMode.REQUIRED)
	public Boolean isValidated;
	
	@Schema(description = "Creation date", requiredMode = Schema.RequiredMode.REQUIRED)
	public String created;
	
	@Schema(description = "Modification date")
	public String modified;
	
	@Schema(description = "Number of members")
	public String nbMembers;

}
