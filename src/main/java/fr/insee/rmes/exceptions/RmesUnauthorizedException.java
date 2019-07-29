package fr.insee.rmes.exceptions;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;

public class RmesUnauthorizedException extends RmesException {
	
	private static final long serialVersionUID = 5611172589954490294L;

	public RmesUnauthorizedException() {
		super(HttpStatus.SC_FORBIDDEN, "Unauthorized", "");
	}
	
	public RmesUnauthorizedException(String message, String details) {
		super(HttpStatus.SC_FORBIDDEN, message, details);
	}

	public RmesUnauthorizedException(String message, JSONArray details) {
		super(HttpStatus.SC_FORBIDDEN, message, details);
	}

}
