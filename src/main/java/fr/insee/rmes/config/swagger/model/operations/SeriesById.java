package fr.insee.rmes.config.swagger.model.operations;

import java.util.List;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import io.swagger.annotations.ApiModelProperty;

public class SeriesById {

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


	@ApiModelProperty(value = "Uri of family")
	public String motherFamily;


	@ApiModelProperty(value = "Label family lg1")
	public String motherFamilyLabelLg1;


	@ApiModelProperty(value = "Label family lg2")
	public String motherFamilyLabelLg2;


	@ApiModelProperty(value = "Operations")
	public List<IdLabelTwoLangs> operations;

	@ApiModelProperty(value = "Type's notation")
	public String typeCode;

	@ApiModelProperty(value = "Type list's notation")
	public String typeList;

	@ApiModelProperty(value = "Frequency's notation")
	public String accrualPeriodicityCode;

	@ApiModelProperty(value = "Frequencies list's notation")
	public String accrualPeriodicityList;

	@ApiModelProperty(value = "Identifier of creator")
	public String creator;

	@ApiModelProperty(value = "Identifier of contributor")
	public String contributor;

}
