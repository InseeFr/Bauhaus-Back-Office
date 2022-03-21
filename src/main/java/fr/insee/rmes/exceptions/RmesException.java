package fr.insee.rmes.exceptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public class RmesException extends Exception {

	private static final String CODE = "code";

	private static final String DETAILS_STRING = "details";

	private static final String MESSAGE = "message";

	private static final long serialVersionUID = -7959158367542389147L;

	private final int status;
	private final String details;

	/**
	 *
	 * @param status
	 * @param message
	 * @param details
	 */
	public RmesException(int status, String message, String details) {
		super();
		this.status = status;
		this.details = createDetails(null, message, details);		
	}

	public RmesException(int status, String message, JSONArray details) {
		super();
		this.status = status;
		this.details = createDetails(null, message, details.toString());
	}

	public RmesException(int status, int errorCode, String message, String details) {
		super();
		this.status = status;
		this.details = createDetails(errorCode, message, details);
	}

	public RmesException(int status, int errorCode, String details) {
		super();
		this.status = status;
		this.details =  createDetails(errorCode, null, details);
	}
		
	public RmesException(int status, int errorCode, JSONArray details) {
		super();
		this.status = status;
		this.details =  createDetails(errorCode, null, details.toString());
	}

	public RmesException(int status, int errorCode, String message, JSONArray details) {
		super();
		this.status = status;
		this.details =  createDetails(errorCode, message, details.toString());
	}
	
	public RmesException(int status, int errorCode, String message, JSONObject details) {
		super();
		this.status = status;
		JSONObject det = details;
		det.put(CODE, errorCode);
		det.put(MESSAGE, message);
		this.details=det.toString();
	}

	public RmesException(HttpStatus status, String message, String details) {
		super();
		this.status = status.value();
		this.details = createDetails(null, message, details);
	}

	public RestMessage toRestMessage(){
		return new RestMessage(this.status, this.getMessage(), this.details);
	}

	public int getStatus() {
		return status;
	}

	public String getDetails() {
		return details;
	}

	public String getMessageAndDetails2() {
		return getMessage() + " " + details;
	}
	
	private String createDetails(Integer errorCode, String message, String detailsParam) {
		JSONObject det = new JSONObject();
		if (errorCode != null) det.put(CODE, errorCode);
		if (message != null) det.put(MESSAGE, message);
		if (details != null) det.put(DETAILS_STRING, detailsParam);
		return det.toString();
		
	}
	
}
