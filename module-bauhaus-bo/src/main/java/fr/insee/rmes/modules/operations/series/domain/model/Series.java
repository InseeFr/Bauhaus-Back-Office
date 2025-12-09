package fr.insee.rmes.modules.operations.series.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import fr.insee.rmes.Constants;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.model.links.OperationsLink;

import java.util.List;

public class Series {

	public String id;

	public String prefLabelLg1;

	public String prefLabelLg2;

	public String altLabelLg1;

	public String altLabelLg2;

	public String abstractLg1;

	public String abstractLg2;
	
	public String historyNoteLg1;

	public String historyNoteLg2;

	public IdLabelTwoLangs family;

	public List<IdLabelTwoLangs> operations;

	public String typeCode;

	public String typeList;

	public String accrualPeriodicityCode;

	public String accrualPeriodicityList;

	public List<OperationsLink> publishers;

	public List<OperationsLink> contributors;

	public List<OperationsLink> dataCollectors;

	@JsonFormat(shape = Shape.ARRAY)
	public List<String> creators;
	
	public List<OperationsLink> seeAlso;

	public List<OperationsLink> replaces;
		
	public List<OperationsLink> generates;
		
	public List<OperationsLink> isReplacedBy;
	
	public String idSims;

	private String created;

	private String updated;
	
	/*
	 * Getters
	 */
	


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
	public IdLabelTwoLangs getFamily() {
		return family;
	}

	public List<IdLabelTwoLangs> getOperations() {
		return operations;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public String getTypeList() {
		return typeList;
	}

	public String getAccrualPeriodicityCode() {
		return accrualPeriodicityCode;
	}

	public String getAccrualPeriodicityList() {
		return accrualPeriodicityList;
	}

	public List<OperationsLink> getContributors() {
		return contributors;
	}

	public List<OperationsLink> getDataCollectors() {
		return dataCollectors;
	}

	public List<String> getCreators() {
		return creators;
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

	public String getIdSims() {
		return idSims;
	}

	public List<OperationsLink> getPublishers() {
		return publishers;
	}

	public List<OperationsLink> getGenerates() {
		return generates;
	}

	public void setGenerates(List<OperationsLink> generates) {
		this.generates = generates;
	}

	public void setId(String id) {
		this.id = id;
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

	public void setFamily(IdLabelTwoLangs family) {
		this.family = family;
	}

	public void setOperations(List<IdLabelTwoLangs> operations) {
		this.operations = operations;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public void setTypeList(String typeList) {
		this.typeList = typeList;
	}

	public void setAccrualPeriodicityCode(String accrualPeriodicityCode) {
		this.accrualPeriodicityCode = accrualPeriodicityCode;
	}

	public void setAccrualPeriodicityList(String accrualPeriodicityList) {
		this.accrualPeriodicityList = accrualPeriodicityList;
	}

	public void setPublishers(List<OperationsLink> publishers) {
		this.publishers = publishers;
	}

	
	public void setContributors(List<OperationsLink> contributors) {
		this.contributors = contributors;
	}

	public void setDataCollectors(List<OperationsLink> dataCollectors) {
		this.dataCollectors = dataCollectors;
	}

	public void setCreators(List<String> creators) {
		this.creators = creators;
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

	public void setIdSims(String idSims) {
		this.idSims = idSims;
	}


	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}
}
