package fr.insee.rmes.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.apache.http.HttpStatus;

public class RmesNotFoundException extends RmesException {

	private static final long serialVersionUID = 1L;

	public RmesNotFoundException(String message, String details) {
		super(HttpStatus.SC_NOT_FOUND, message, details);
	}

	/**
	 * Le code d'erreur est exposé dans le corps de la réponse, comme pour les 400 : le front
	 * s'en sert pour retrouver le libellé traduit, il ne sait pas lire un préfixe dans le message.
	 */
	public RmesNotFoundException(int errorCode, String message, String details) {
		super(HttpStatus.SC_NOT_FOUND, errorCode, message, details);
	}
	public RmesNotFoundException(String message) {
		super(HttpStatus.SC_NOT_FOUND, message, "Not found");
	}
	
}
