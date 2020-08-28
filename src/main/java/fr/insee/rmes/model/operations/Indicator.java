package fr.insee.rmes.model.operations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.links.OperationsLink;
import io.swagger.v3.oas.annotations.media.Schema;

public class Indicator {

	@Schema(description = "Id", required = true)
	public String id;

	@Schema(description = "Label lg1", required = true)
	public String prefLabelLg1;

	@Schema(description = "Label lg2")
	public String prefLabelLg2;

	@Schema(description = "Alternative label Lg1")
	public String altLabelLg1;

	@Schema(description = "Alternative label Lg2")
	public String altLabelLg2;

	@Schema(description = "Abstract Lg1")
	public String abstractLg1;

	@Schema(description = "Abstract lg2")
	public String abstractLg2;

	@Schema(description = "History note Lg1")
	public String historyNoteLg1;

	@Schema(description = "History note lg2")
	public String historyNoteLg2;
	
	@Schema(description = "Frequency's notation")
	public String accrualPeriodicityCode;

	@Schema(description = "Frequencies list's notation")
	public String accrualPeriodicityList;

	@JsonFormat(shape = Shape.ARRAY)
	@Schema(description = "Identifier of publishers")
	public List<String> publishers;

	@Schema(description = "Identifiers of contributors")
	public List<OperationsLink> contributors;
	
	@Schema(description = "Identifiers of creators")
	@JsonFormat(shape = Shape.ARRAY)
	public List<String> creators;

	@Schema(description = "List of resources to see also")
	public List<OperationsLink> seeAlso;

	@Schema(description = "List of resources that replaces the series")
	public List<OperationsLink> replaces;
	
	@Schema(description = "List of resources replaced by the series")
	public List<OperationsLink> isReplacedBy;
	
	@Schema(description = "List of resources which generate the indicator")
	public List<OperationsLink> wasGeneratedBy;
	
	public String idSims;

	public Indicator(String id) {
		this.id=id;
	}
	
	public Indicator() throws RmesException {
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


	public List<String> getPublishers() {
		return publishers;
	}


	public List<OperationsLink> getContributors() {
		return contributors;
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

	public String getIdSims() {
		return idSims;
	}

	public List<String> getCreators() {
		return creators;
	}

	public void setPublishers(List<String> publishers) {
		this.publishers = publishers;
	}
	
	public void setCreators(List<String> creators) {
		this.creators = creators;
	}

	public void setPrefLabelLg1(String prefLabelLg1) {
		this.prefLabelLg1 = prefLabelLg1;
	}

	public void setPrefLabelLg2(String prefLabelLg2) {
		this.prefLabelLg2 = prefLabelLg2;
	}

	public void setAltLabelLg1(String altLabelLg1) {
		this.altLabelLg1 = altLabelLg1;
	}

	public void setAltLabelLg2(String altLabelLg2) {
		this.altLabelLg2 = altLabelLg2;
	}

	public void setAbstractLg1(String abstractLg1) {
		this.abstractLg1 = abstractLg1;
	}

	public void setAbstractLg2(String abstractLg2) {
		this.abstractLg2 = abstractLg2;
	}

	public void setHistoryNoteLg1(String historyNoteLg1) {
		this.historyNoteLg1 = historyNoteLg1;
	}

	public void setHistoryNoteLg2(String historyNoteLg2) {
		this.historyNoteLg2 = historyNoteLg2;
	}

	public void setAccrualPeriodicityCode(String accrualPeriodicityCode) {
		this.accrualPeriodicityCode = accrualPeriodicityCode;
	}

	public void setAccrualPeriodicityList(String accrualPeriodicityList) {
		this.accrualPeriodicityList = accrualPeriodicityList;
	}

	public void setContributors(List<OperationsLink> contributors) {
		this.contributors = contributors;
	}

	public void setSeeAlso(List<OperationsLink> seeAlso) {
		this.seeAlso = seeAlso;
	}

	public void setReplaces(List<OperationsLink> replaces) {
		this.replaces = replaces;
	}

	public void setIsReplacedBy(List<OperationsLink> isReplacedBy) {
		this.isReplacedBy = isReplacedBy;
	}

	public void setWasGeneratedBy(List<OperationsLink> wasGeneratedBy) {
		this.wasGeneratedBy = wasGeneratedBy;
	}

	public void setIdSims(String idSims) {
		this.idSims = idSims;
	}

}
