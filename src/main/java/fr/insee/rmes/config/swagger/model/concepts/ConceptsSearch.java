package fr.insee.rmes.config.swagger.model.concepts;

import fr.insee.rmes.persistance.disseminationStatus.DisseminationStatus;
import io.swagger.annotations.ApiModelProperty;

public class ConceptsSearch {
	
	@ApiModelProperty(value = "Id", required = true)
	public String id;
	
	@ApiModelProperty(value = "Label", required = true)
	public String label;
	
	@ApiModelProperty(value = "Owner", required = true)
	public String creator;
	
	@ApiModelProperty(value = "Dissemination status", required = true)
	public DisseminationStatus disseminationStatus;
	
	@ApiModelProperty(value = "Validation status", required = true)
	public Boolean validationStatus;
	
	@ApiModelProperty(value = "Definition")
	public String definition;
	
	@ApiModelProperty(value = "Creation date", required = true)
	public String created;
	
	@ApiModelProperty(value = "Modification date")
	public String modified;
	
	@ApiModelProperty(value = "Expiration date")
	public String valid;

}
