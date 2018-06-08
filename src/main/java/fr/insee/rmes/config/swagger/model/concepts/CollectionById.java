package fr.insee.rmes.config.swagger.model.concepts;

import io.swagger.annotations.ApiModelProperty;

public class CollectionById {
	
	@ApiModelProperty(value = "Id", required = true)
	public String id;
	
	@ApiModelProperty(value = "Label lg1", required = true)
	public String prefLabelLg1;
	
	@ApiModelProperty(value = "Label lg2")
	public String prefLabelLg2;
	
	@ApiModelProperty(value = "Description lg1")
	public String descriptionLg1;
	
	@ApiModelProperty(value = "Description lg2")
	public String descriptionLg2;
	
	@ApiModelProperty(value = "Owner", required = true)
	public String creator;
	
	@ApiModelProperty(value = "Contributor", required = true)
	public String contributor;
	
	@ApiModelProperty(value = "Is concept validated", required = true)
	public Boolean isValidated;
	
	@ApiModelProperty(value = "Creation date", required = true)
	public String created;
	
	@ApiModelProperty(value = "Modification date")
	public String modified;

}
