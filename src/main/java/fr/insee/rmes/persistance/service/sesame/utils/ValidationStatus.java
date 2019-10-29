package fr.insee.rmes.persistance.service.sesame.utils;

public enum ValidationStatus {
	MODIFIED ("Modified"),
	UNPUBLISHED ("Unpublished"),
	VALIDATED ("Validated");
	
	private String value;
	
	private ValidationStatus(String value) {
        this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public String toString() {
		return value;
	}
	
	private void setValue(String value) {
		this.value = value;
	}

}
