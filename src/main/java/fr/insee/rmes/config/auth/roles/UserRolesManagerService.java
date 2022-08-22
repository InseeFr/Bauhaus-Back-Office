package fr.insee.rmes.config.auth.roles;

import fr.insee.rmes.exceptions.RmesException;

public interface UserRolesManagerService {
	
	public String getRoles() throws RmesException;
	
	public String checkSugoiConnexion() throws RmesException;
	
}
