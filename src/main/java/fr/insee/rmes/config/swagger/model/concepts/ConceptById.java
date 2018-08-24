package fr.insee.rmes.config.swagger.model.concepts;

import fr.insee.rmes.persistance.disseminationStatus.DisseminationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class ConceptById {
	
	@Schema(description = "Id", required = true)
	public String id;
	
	@Schema(description = "Label lg1", required = true)
	public String prefLabelLg1;
	
	@Schema(description = "Label lg2")
	public String prefLabelLg2;
	
	@Schema(description = "Alternative label lg1")
	public String altLabelLg1;
	
	@Schema(description = "Alternative label lg2")
	public String altLabelLg2;
	
	@Schema(description = "Owner", required = true)
	public String creator;
	
	@Schema(description = "Contributor", required = true)
	public String contributor;
	
	@Schema(description = "Dissemination status", required = true)
	public DisseminationStatus disseminationStatus;
	
	@Schema(description = "Is concept validated", required = true)
	public Boolean isValidated;
	
	@Schema(description = "Additional material")
	public String additionalMaterial;
	
	@Schema(description = "Concept version")
	public String conceptVersion;
	
	@Schema(description = "Creation date", required = true)
	public String created;
	
	@Schema(description = "Modification date")
	public String modified;
	
	@Schema(description = "Expiration date")
	public String valid;

}
