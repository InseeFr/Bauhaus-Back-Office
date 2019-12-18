package fr.insee.rmes.exceptions;

import org.json.JSONArray;
import org.json.JSONObject;

public class RmesException extends Exception {

	private static final long serialVersionUID = -7959158367542389147L;

	private final int status;
	private final JSONObject details;

	/**
	 *
	 * @param status
	 * @param message
	 * @param details
	 */
	public RmesException(int status, String message, String details) {
		//super(message);
		super();
		this.status = status;
		this.details = new JSONObject();
		this.details.put("message", message);
		this.details.put("details",	details);		
	}

	public RmesException(int status, String message, JSONArray details) {
		//super(message);
		super();
		this.status = status;
		this.details = new JSONObject();
		this.details.put("message", message);
		this.details.put("details", details.toString());
	}

	public RmesException(int status, int errorCode, String message, String details) {
		//super(errorCode+":"+message);
		super();
		this.status = status;
		this.details = new JSONObject();
		this.details.put("code", errorCode);
		this.details.put("message", message);
		this.details.put("details", details);
	}

	public RmesException(int status, int errorCode, String details) {
		super();
		this.status = status;
		this.details = new JSONObject();
		this.details.put("code", errorCode);
		this.details.put("details", details.toString());
	}

	public RmesException(int status, int errorCode, JSONArray details) {
		super();
		this.status = status;
		this.details = new JSONObject();
		this.details.put("code", errorCode);
		this.details.put("details", details.toString());
	}

	public RmesException(int status, int errorCode, String message, JSONArray details) {
		// super(errorCode + ":" + message);
		super();
		this.status = status;
		this.details = new JSONObject();
		this.details.put("code", errorCode);
		this.details.put("message", message);
		this.details.put("details", details.toString());
	}

	public RestMessage toRestMessage(){
		return new RestMessage(this.status, this.getMessage(), this.details.toString());
	}

	public int getStatus() {
		return status;
	}

	public String getMessageAndDetails() {
		return getMessage() + " " + details;
	}

}
