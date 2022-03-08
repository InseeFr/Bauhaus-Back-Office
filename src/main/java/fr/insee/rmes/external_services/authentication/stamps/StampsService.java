package fr.insee.rmes.external_services.authentication.stamps;

import fr.insee.rmes.exceptions.RmesException;

public interface StampsService {
	
	public String getStamp() throws RmesException;
	
	public String getStampsApiRH() throws RmesException;
	

}
