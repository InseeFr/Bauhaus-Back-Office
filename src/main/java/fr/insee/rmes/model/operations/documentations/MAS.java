package fr.insee.rmes.model.operations.documentations;

import io.swagger.v3.oas.annotations.media.Schema;

public class MAS {

	@Schema(description="Id of the Metadata Attribute Specification")
	private String idMas;
	private String masLabelLg1;
	private String masLabelLg2;
	private String idParent;
	private Boolean isPresentational;
	
	
	public String getIdMas() {
		return idMas;
	}
	public void setIdMas(String idMas) {
		this.idMas = idMas;
	}
	public String getMasLabelLg1() {
		return masLabelLg1;
	}
	public void setMasLabelLg1(String masLabelLg1) {
		this.masLabelLg1 = masLabelLg1;
	}
	public String getMasLabelLg2() {
		return masLabelLg2;
	}
	public void setMasLabelLg2(String masLabelLg2) {
		this.masLabelLg2 = masLabelLg2;
	}
	public String getIdParent() {
		return idParent;
	}
	public void setIdParent(String idParent) {
		this.idParent = idParent;
	}
	public Boolean getIsPresentational() {
		return isPresentational;
	}
	public void setIsPresentational(Boolean isPresentational) {
		this.isPresentational = isPresentational;
	}
}
