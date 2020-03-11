package fr.insee.rmes.persistance.service.sesame.operations.indicators;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.links.OperationsLink;
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

	@Schema(description = "Identifier of creator")
	public String creator;

	@Schema(description = "Identifiers of contributors")
	public List<OperationsLink> contributor;
	
	@Schema(description = "Identifier of gestionnaire")
	@JsonFormat(shape = Shape.ARRAY)
	public List<String> gestionnaires;

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


	public String getCreator() {
		return creator;
	}


	public List<OperationsLink> getContributor() {
		return contributor;
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

	public List<String> getGestionnaires() {
		return gestionnaires;
	}

	public void setGestionnaires(List<String> gestionnaires) {
		this.gestionnaires = gestionnaires;
	}

}
