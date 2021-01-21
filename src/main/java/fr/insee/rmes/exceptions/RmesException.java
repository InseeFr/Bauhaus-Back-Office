package fr.insee.rmes.exceptions;

import org.json.JSONArray;
import org.json.JSONObject;

public class RmesException extends Exception {

	private static final String CODE = "code";

	private static final String DETAILS_STRING = "details";

	private static final String MESSAGE = "message";

	private static final long serialVersionUID = -7959158367542389147L;

	private final int status;
	private final transient JSONObject details;

	/**
	 *
	 * @param status
	 * @param message
	 * @param details
	 */
	public RmesException(int status, String message, String details) {
		super();
		this.status = status;
		this.details = new Details();
		this.details.put(MESSAGE, message);
		this.details.put(DETAILS_STRING,	details);		
	}

	public RmesException(int status, String message, JSONArray details) {
		super();
		this.status = status;
		this.details = new Details();
		this.details.put(MESSAGE, message);
		this.details.put(DETAILS_STRING, details.toString());
	}

	public RmesException(int status, int errorCode, String message, String details) {
		super();
		this.status = status;
		this.details = new Details();
		this.details.put(CODE, errorCode);
		this.details.put(MESSAGE, message);
		this.details.put(DETAILS_STRING, details);
	}

	public RmesException(int status, int errorCode, String details) {
		super();
		this.status = status;
		this.details = new Details();
		this.details.put(CODE, errorCode);
		this.details.put(DETAILS_STRING, details);
	}
		
	public RmesException(int status, int errorCode, JSONArray details) {
		super();
		this.status = status;
		this.details = new Details();
		this.details.put(CODE, errorCode);
		this.details.put(DETAILS_STRING, details.toString());
	}

	public RmesException(int status, int errorCode, String message, JSONArray details) {
		super();
		this.status = status;
		this.details = new Details();
		this.details.put(CODE, errorCode);
		this.details.put(MESSAGE, message);
		this.details.put(DETAILS_STRING, details.toString());
	}
	
	public RmesException(int status, int errorCode, String message, JSONObject details) {
		super();
		this.status = status;
		this.details = details;
		this.details.put(CODE, errorCode);
		this.details.put(MESSAGE, message);
	}

	public RestMessage toRestMessage(){
		return new RestMessage(this.status, this.getMessage(), this.details.toString());
	}

	public int getStatus() {
		return status;
	}

	public String getDetails() {
		return details.toString();
	}

	public String getMessageAndDetails2() {
		return getMessage() + " " + details;
	}
	
}
