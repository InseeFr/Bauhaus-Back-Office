package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.v3.oas.annotations.media.Schema;

public class CollectionById {
	
	@Schema(description = "Id", required = true)
	public String id;
	
	@Schema(description = "Label lg1", required = true)
	public String prefLabelLg1;
	
	@Schema(description = "Label lg2")
	public String prefLabelLg2;
	
	@Schema(description = "Description lg1")
	public String descriptionLg1;
	
	@Schema(description = "Description lg2")
	public String descriptionLg2;
	
	@Schema(description = "Owner", required = true)
	public String creator;
	
	@Schema(description = "Contributor", required = true)
	public String contributor;
	
	@Schema(description = "Is concept validated", required = true)
	public Boolean isValidated;
	
	@Schema(description = "Creation date", required = true)
	public String created;
	
	@Schema(description = "Modification date")
	public String modified;

}
