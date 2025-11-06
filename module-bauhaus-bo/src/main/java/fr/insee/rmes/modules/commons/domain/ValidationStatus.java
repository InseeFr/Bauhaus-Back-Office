package fr.insee.rmes.modules.commons.domain;

public enum ValidationStatus {
	MODIFIED ("Modified"),
	UNPUBLISHED ("Unpublished"),
	VALIDATED ("Validated");
	
	private String value;
	
	ValidationStatus(String value) {
        this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
	
	private void setValue(String value) {
		this.value = value;
	}

}
