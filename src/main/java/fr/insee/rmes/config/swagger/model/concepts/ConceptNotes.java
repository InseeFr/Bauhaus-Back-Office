package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.annotations.ApiModelProperty;

public class ConceptNotes {
	
	@ApiModelProperty(value = "Definition lg1")
	public String definitionLg1;
	
	@ApiModelProperty(value = "Definition lg2")
	public String definitionLg2;
	
	@ApiModelProperty(value = "Scope note lg1")
	public String scopeNoteLg1;
	
	@ApiModelProperty(value = "Scope note  lg2")
	public String scopeNoteLg2;
	
	@ApiModelProperty(value = "Editorial note lg1")
	public String editorialNoteLg1;
	
	@ApiModelProperty(value = "Editorial note lg2")
	public String editorialNoteLg2;
	
	@ApiModelProperty(value = "Change note lg1")
	public String changeNoteLg1;
	
	@ApiModelProperty(value = "Change note lg2")
	public String changeNoteLg2;

}
