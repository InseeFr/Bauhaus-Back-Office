package fr.insee.rmes.config.swagger.model.classifications;

import io.swagger.v3.oas.annotations.media.Schema;

public class FamilyClass {
	
	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String prefLabelLg1;

}
