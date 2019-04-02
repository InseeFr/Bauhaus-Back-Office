package fr.insee.rmes.exceptions;

//private static final long serialVersionUID = ?? ;

public class RmesNotFoundException extends RmesException {

	public RmesNotFoundException(String message, String details) {
		super(404, message, details);
	}

}
