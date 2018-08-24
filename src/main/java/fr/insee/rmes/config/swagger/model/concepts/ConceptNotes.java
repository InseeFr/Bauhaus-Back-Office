package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.v3.oas.annotations.media.Schema;

public class ConceptNotes {
	
	@Schema(description = "Definition lg1")
	public String definitionLg1;
	
	@Schema(description = "Definition lg2")
	public String definitionLg2;
	
	@Schema(description = "Scope note lg1")
	public String scopeNoteLg1;
	
	@Schema(description = "Scope note  lg2")
	public String scopeNoteLg2;
	
	@Schema(description = "Editorial note lg1")
	public String editorialNoteLg1;
	
	@Schema(description = "Editorial note lg2")
	public String editorialNoteLg2;
	
	@Schema(description = "Change note lg1")
	public String changeNoteLg1;
	
	@Schema(description = "Change note lg2")
	public String changeNoteLg2;

}
