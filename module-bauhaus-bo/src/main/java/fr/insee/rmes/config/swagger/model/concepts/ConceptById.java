package fr.insee.rmes.config.swagger.model.concepts;

import fr.insee.rmes.model.dissemination_status.DisseminationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class ConceptById {
	
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String prefLabelLg1;
	
	@Schema(description = "Label lg2")
	public String prefLabelLg2;
	
	@Schema(description = "Alternative value lg1")
	public String altLabelLg1;
	
	@Schema(description = "Alternative value lg2")
	public String altLabelLg2;
	
	@Schema(description = "Owner", requiredMode = Schema.RequiredMode.REQUIRED)
	public String creator;
	
	@Schema(description = "Contributor", requiredMode = Schema.RequiredMode.REQUIRED)
	public String contributor;
	
	@Schema(description = "Dissemination status", requiredMode = Schema.RequiredMode.REQUIRED)
	public DisseminationStatus disseminationStatus;
	
	@Schema(description = "Is concept validated", requiredMode = Schema.RequiredMode.REQUIRED)
	public Boolean isValidated;
	
	@Schema(description = "Additional material")
	public String additionalMaterial;
	
	@Schema(description = "Concept version")
	public String conceptVersion;
	
	@Schema(description = "Creation date", requiredMode = Schema.RequiredMode.REQUIRED)
	public String created;
	
	@Schema(description = "Modification date")
	public String modified;
	
	@Schema(description = "Expiration date")
	public String valid;

}
