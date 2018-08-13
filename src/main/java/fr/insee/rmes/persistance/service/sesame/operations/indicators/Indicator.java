package fr.insee.rmes.persistance.service.sesame.operations.indicators;

import java.util.List;

import fr.insee.rmes.persistance.service.sesame.links.OperationsLink;
import io.swagger.annotations.ApiModelProperty;

public class Indicator {

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


	@ApiModelProperty(value = "History note Lg1")
	public String historyNoteLg1;


	@ApiModelProperty(value = "History note lg2")
	public String historyNoteLg2;
	
	@ApiModelProperty(value = "Frequency's notation")
	public String accrualPeriodicityCode;

	@ApiModelProperty(value = "Frequencies list's notation")
	public String accrualPeriodicityList;

	@ApiModelProperty(value = "Identifier of creator")
	public String creator;

	@ApiModelProperty(value = "Identifier of stake holder")
	public List<OperationsLink> stakeHolder;

	@ApiModelProperty(value = "List of resources to see also")
	public List<OperationsLink> seeAlso;

	@ApiModelProperty(value = "List of resources that replaces the series")
	public List<OperationsLink> replaces;
	
	@ApiModelProperty(value = "List of resources replaced by the series")
	public List<OperationsLink> isReplacedBy;
	
	@ApiModelProperty(value = "List of resources which generate the indicator")
	public List<OperationsLink> wasGeneratedBy;
	

	public Indicator(String id) {
		this.id=id;
	}
	
	public Indicator() {
		this.id= new IndicatorsUtils().createID();
	}


	public String getId() {
		return id;
	}


	public String getPrefLabelLg1() {
		return prefLabelLg1;
	}


	public String getPrefLabelLg2() {
		return prefLabelLg2;
	}


	public String getAltLabelLg1() {
		return altLabelLg1;
	}


	public String getAltLabelLg2() {
		return altLabelLg2;
	}


	public String getAbstractLg1() {
		return abstractLg1;
	}


	public String getAbstractLg2() {
		return abstractLg2;
	}


	public String getHistoryNoteLg1() {
		return historyNoteLg1;
	}


	public String getHistoryNoteLg2() {
		return historyNoteLg2;
	}


	public String getAccrualPeriodicityCode() {
		return accrualPeriodicityCode;
	}


	public String getAccrualPeriodicityList() {
		return accrualPeriodicityList;
	}


	public String getCreator() {
		return creator;
	}


	public List<OperationsLink> getStakeHolder() {
		return stakeHolder;
	}


	public List<OperationsLink> getSeeAlso() {
		return seeAlso;
	}


	public List<OperationsLink> getReplaces() {
		return replaces;
	}


	public List<OperationsLink> getIsReplacedBy() {
		return isReplacedBy;
	}


	public List<OperationsLink> getWasGeneratedBy() {
		return wasGeneratedBy;
	}

	

}
