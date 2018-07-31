package fr.insee.rmes.config.swagger.model.operations;

import java.util.List;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import io.swagger.annotations.ApiModelProperty;

public class FamilyById {

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

	@ApiModelProperty(value = "Abstract lg1, description")
	public String abstractLg1;


	@ApiModelProperty(value = "Abstract lg2")
	public String abstractLg2;

	@ApiModelProperty(value = "Subjects, Topics")
	public List<IdLabelTwoLangs> subjects;

	@ApiModelProperty(value = "Series")
	public List<IdLabelTwoLangs> series;

}
