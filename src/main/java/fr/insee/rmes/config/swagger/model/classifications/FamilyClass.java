package fr.insee.rmes.config.swagger.model.classifications;

import io.swagger.v3.oas.annotations.media.Schema;

public class FamilyClass {
	
	@Schema(description = "Label lg1", required = true)
	public String prefLabelLg1;

}
