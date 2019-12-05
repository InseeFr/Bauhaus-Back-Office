package fr.insee.rmes.exceptions;

import org.apache.commons.httpclient.HttpStatus;

public class RmesNotFoundException extends RmesException {

	private static final long serialVersionUID = 1L;

	public RmesNotFoundException(String message, String details) {
		super(HttpStatus.SC_NOT_FOUND, message, details);
	}

	public RmesNotFoundException(int errorCode, String message, String details) {
		super(HttpStatus.SC_NOT_FOUND, errorCode + " : " + message, details);
	}
	
}
