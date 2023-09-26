package fr.insee.rmes.external_services.authentication.stamps;

import fr.insee.rmes.exceptions.RmesException;

import java.util.List;

public interface StampsService {
	
	List<String> getStamps() throws RmesException;

	String findStampFrom(Object principal) throws RmesException;
}
