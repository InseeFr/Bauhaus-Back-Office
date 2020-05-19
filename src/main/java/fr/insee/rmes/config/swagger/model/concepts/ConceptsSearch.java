package fr.insee.rmes.config.swagger.model.concepts;

import fr.insee.rmes.model.dissemination_status.DisseminationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class ConceptsSearch {
	
	@Schema(description = "Id", required = true)
	public String id;
	
	@Schema(description = "Label", required = true)
	public String label;
	
	@Schema(description = "Owner", required = true)
	public String creator;
	
	@Schema(description = "Dissemination status", required = true)
	public DisseminationStatus disseminationStatus;
	
	@Schema(description = "Validation status", required = true)
	public Boolean validationStatus;
	
	@Schema(description = "Definition")
	public String definition;
	
	@Schema(description = "Creation date", required = true)
	public String created;
	
	@Schema(description = "Modification date")
	public String modified;
	
	@Schema(description = "Expiration date")
	public String valid;

}
