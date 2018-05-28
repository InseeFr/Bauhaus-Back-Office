package fr.insee.rmes.exceptions;

public class RmesUnauthorizedException extends RmesException {
	
	private static final long serialVersionUID = 5611172589954490294L;

	public RmesUnauthorizedException() {
		super(403, "Unauthorized", "");
	}

}
