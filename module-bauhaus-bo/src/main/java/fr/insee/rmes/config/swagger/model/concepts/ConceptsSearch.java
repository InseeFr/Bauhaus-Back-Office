package fr.insee.rmes.config.swagger.model.concepts;

import fr.insee.rmes.modules.commons.domain.model.DisseminationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class ConceptsSearch {
	
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label", requiredMode = Schema.RequiredMode.REQUIRED)
	public String label;
	
	@Schema(description = "Owner", requiredMode = Schema.RequiredMode.REQUIRED)
	public String creator;
	
	@Schema(description = "Dissemination status", requiredMode = Schema.RequiredMode.REQUIRED)
	public DisseminationStatus disseminationStatus;
	
	@Schema(description = "Validation status", requiredMode = Schema.RequiredMode.REQUIRED)
	public Boolean validationStatus;
	
	@Schema(description = "Definition")
	public String definition;
	
	@Schema(description = "Creation date", requiredMode = Schema.RequiredMode.REQUIRED)
	public String created;
	
	@Schema(description = "Modification date")
	public String modified;
	
	@Schema(description = "Expiration date")
	public String valid;

}
