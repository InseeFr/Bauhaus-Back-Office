package fr.insee.rmes.external.services.authentication.stamps;

import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.exceptions.RmesException;

import java.util.List;

public interface StampsService {
	
	List<String> getStamps() throws RmesException;

	Stamp findStampFrom(Object principal) throws RmesException;
}
