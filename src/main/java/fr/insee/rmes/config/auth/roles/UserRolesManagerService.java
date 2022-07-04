package fr.insee.rmes.config.auth.roles;

import fr.insee.rmes.exceptions.RmesException;

public interface UserRolesManagerService {
	
	public String getRoles() throws RmesException;
	
	public String getAgentsSugoi() throws RmesException ;
	
	public void setAddRole(String role, String user) throws  RmesException ;
	
	public void setDeleteRole(String roles, String user) throws  RmesException;

	public String checkSugoiConnexion() throws RmesException;
	
}
