package fr.insee.rmes.exceptions;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

public class RmesBadRequestException extends RmesException {

	private static final long serialVersionUID = 400L;

	public RmesBadRequestException(String message) {
		super(HttpStatus.SC_BAD_REQUEST, message, "");
	}
	
	public RmesBadRequestException(String message, String details) {
		super(HttpStatus.SC_BAD_REQUEST, message, details);
	}

	public RmesBadRequestException(String message, JSONArray details) {
		super(HttpStatus.SC_BAD_REQUEST, message, details);
	}	
	
	public RmesBadRequestException(int errorCode, String message, String details) {
		super(HttpStatus.SC_BAD_REQUEST, errorCode, message, details);
	}

	public RmesBadRequestException(int errorCode, String message, JSONArray details) {
		super(HttpStatus.SC_BAD_REQUEST, errorCode, message, details);
	}

	public RmesBadRequestException(int errorCode, String message) {
		super(HttpStatus.SC_BAD_REQUEST, errorCode, message, "");
	}
	public RmesBadRequestException(int errorCode, String message, JSONObject details) {
		super(HttpStatus.SC_FORBIDDEN, errorCode, message, details);	}
}
