package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.v3.oas.annotations.media.Schema;

public class CollectionById {
	
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String prefLabelLg1;
	
	@Schema(description = "Label lg2")
	public String prefLabelLg2;
	
	@Schema(description = "Description lg1")
	public String descriptionLg1;
	
	@Schema(description = "Description lg2")
	public String descriptionLg2;
	
	@Schema(description = "Owner", requiredMode = Schema.RequiredMode.REQUIRED)
	public String creator;
	
	@Schema(description = "Contributor", requiredMode = Schema.RequiredMode.REQUIRED)
	public String contributor;
	
	@Schema(description = "Is concept validated", requiredMode = Schema.RequiredMode.REQUIRED)
	public Boolean isValidated;
	
	@Schema(description = "Creation date", requiredMode = Schema.RequiredMode.REQUIRED)
	public String created;
	
	@Schema(description = "Modification date")
	public String modified;

}
