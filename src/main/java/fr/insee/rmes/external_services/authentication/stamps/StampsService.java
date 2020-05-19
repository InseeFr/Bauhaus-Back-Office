package fr.insee.rmes.external_services.authentication.stamps;

import fr.insee.rmes.exceptions.RmesException;

public interface StampsService {
	
	public String getStamps() throws RmesException;

}
