package fr.insee.rmes.config.swagger.model.operations;

import java.util.List;

import fr.insee.rmes.persistance.service.sesame.links.OperationsLink;
import io.swagger.annotations.ApiModelProperty;

public class IndicatorById {

	@ApiModelProperty(value = "Id", required = true)
	public String id;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String prefLabelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String prefLabelLg2;

	@ApiModelProperty(value = "Alternative label Lg1")
	public String altLabelLg1;

	@ApiModelProperty(value = "Alternative label Lg2")
	public String altLabelLg2;

	@ApiModelProperty(value = "Abstract Lg1")
	public String abstractLg1;


	@ApiModelProperty(value = "Abstract lg2")
	public String abstractLg2;

	//TODO check
	@ApiModelProperty(value = "Frequency's notation")
	public String accrualPeriodicityCode;

	@ApiModelProperty(value = "Frequencies list's notation")
	public String accrualPeriodicityList;

	@ApiModelProperty(value = "Identifier of creator")
	public String creator;

	@ApiModelProperty(value = "Identifier of stake holder")
	public String stakeHolder;

	@ApiModelProperty(value = "Linked objects")
	public List<OperationsLink> links;


}
