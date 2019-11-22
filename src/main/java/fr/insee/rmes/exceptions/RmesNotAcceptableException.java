package fr.insee.rmes.exceptions;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;

public class RmesNotAcceptableException extends RmesException {

	private static final long serialVersionUID = 2L;

	public RmesNotAcceptableException(String message, String details) {
		super(HttpStatus.SC_NOT_ACCEPTABLE, message, details);
	}

	public RmesNotAcceptableException(String message, JSONArray details) {
		super(HttpStatus.SC_NOT_ACCEPTABLE, message, details);
	}	
	
	public RmesNotAcceptableException(int errorCode, String message, String details) {
		super(HttpStatus.SC_NOT_ACCEPTABLE, errorCode + " : " + message, details);
	}

	public RmesNotAcceptableException(int errorCode, String message, JSONArray details) {
		super(HttpStatus.SC_NOT_ACCEPTABLE, errorCode + " : " + message, details);
	}	
	
}
