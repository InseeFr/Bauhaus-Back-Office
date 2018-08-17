package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.v3.oas.annotations.media.Schema;

public class ConceptLinks {

	@Schema(description = "Id", required = true)
	public String id;
	
	@Schema(description = "Type of link", required = true)
	public String typeOfLink;
	
	@Schema(description = "Label lg1", required = true)
	public String prefLabelLg1;
	
	@Schema(description = "Label lg2")
	public String prefLabelLg2;

}
