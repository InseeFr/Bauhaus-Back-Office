package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.v3.oas.annotations.media.Schema;

public class CollectionMembers {
	
	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String prefLabelLg1;
	
	@Schema(description = "Label lg2")
	public String prefLabelLg2;

}
