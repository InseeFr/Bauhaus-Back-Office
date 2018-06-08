package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.annotations.ApiModelProperty;

public class CollectionsDashboard {
	
	@ApiModelProperty(value = "Id", required = true)
	public String id;
	
	@ApiModelProperty(value = "Label", required = true)
	public String label;
	
	@ApiModelProperty(value = "Owner", required = true)
	public String creator;
	
	@ApiModelProperty(value = "Is collection validated", required = true)
	public Boolean isValidated;
	
	@ApiModelProperty(value = "Creation date", required = true)
	public String created;
	
	@ApiModelProperty(value = "Modification date")
	public String modified;
	
	@ApiModelProperty(value = "Number of members")
	public String nbMembers;

}
