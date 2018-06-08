package fr.insee.rmes.config.swagger.model.concepts;

import fr.insee.rmes.persistance.disseminationStatus.DisseminationStatus;
import io.swagger.annotations.ApiModelProperty;

public class ConceptById {
	
	@ApiModelProperty(value = "Id", required = true)
	public String id;
	
	@ApiModelProperty(value = "Label lg1", required = true)
	public String prefLabelLg1;
	
	@ApiModelProperty(value = "Label lg2")
	public String prefLabelLg2;
	
	@ApiModelProperty(value = "Alternative label lg1")
	public String altLabelLg1;
	
	@ApiModelProperty(value = "Alternative label lg2")
	public String altLabelLg2;
	
	@ApiModelProperty(value = "Owner", required = true)
	public String creator;
	
	@ApiModelProperty(value = "Contributor", required = true)
	public String contributor;
	
	@ApiModelProperty(value = "Dissemination status", required = true)
	public DisseminationStatus disseminationStatus;
	
	@ApiModelProperty(value = "Is concept validated", required = true)
	public Boolean isValidated;
	
	@ApiModelProperty(value = "Additional material")
	public String additionalMaterial;
	
	@ApiModelProperty(value = "Concept version")
	public String conceptVersion;
	
	@ApiModelProperty(value = "Creation date", required = true)
	public String created;
	
	@ApiModelProperty(value = "Modification date")
	public String modified;
	
	@ApiModelProperty(value = "Expiration date")
	public String valid;

}
