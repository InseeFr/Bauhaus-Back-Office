package fr.insee.rmes.model.operations;

import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import org.springframework.util.ObjectUtils;

public class Operation {

	public String id;

	public String prefLabelLg1;

	public String prefLabelLg2;

	public String altLabelLg1;

	public String altLabelLg2;

	public IdLabelTwoLangs series;
	
	public String idSims;

	private String created;

	private String modified;

	private String validationState;

	private Integer year;

	public Operation(String id, String prefLabelLg1, String prefLabelLg2, String altLabelLg1, String altLabelLg2,
			IdLabelTwoLangs series, String idSims, String validationState, Integer year) {
		super();
		this.id = id;
		this.prefLabelLg1 = prefLabelLg1;
		this.prefLabelLg2 = prefLabelLg2;
		this.altLabelLg1 = altLabelLg1;
		this.altLabelLg2 = altLabelLg2;
		this.series = series;
		this.idSims = idSims;
		this.validationState = validationState;
		this.year = year;
	}

	public Operation() {
		super();
	}

	public Operation(String id) {
		this.id=id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if(!ObjectUtils.isEmpty(id)) {
			this.id = id;
		}
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


	public IdLabelTwoLangs getSeries() {
		return series;
	}


	public String getIdSims() {
		return idSims;
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

	public void setSeries(IdLabelTwoLangs series) {
		this.series = series;
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

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public String getValidationState() {
		return validationState;
	}

	public void setValidationState(String validationState) {
		this.validationState = validationState;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
}
