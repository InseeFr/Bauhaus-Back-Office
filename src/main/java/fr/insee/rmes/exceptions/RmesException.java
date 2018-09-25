package fr.insee.rmes.exceptions;

public class RmesException extends Exception {

	private static final long serialVersionUID = -7959158367542389147L;

	private int status;
    private String details;

    /**
     *
     * @param status
     * @param message
     * @param details
     */
    public RmesException(int status, String message, String details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    public RestMessage toRestMessage(){
        return new RestMessage(this.status, this.getMessage(), this.details);
    }

	public int getStatus() {
		return status;
	}
	
	public String getMessageAndDetails() {
		return getMessage() + " " + details;
	}
	
}
