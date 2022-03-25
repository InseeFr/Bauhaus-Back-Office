package fr.insee.rmes.config.auth.roles;

import fr.insee.rmes.exceptions.RmesException;

public interface UserRolesManagerService {
	

	public String getRoles() throws RmesException;
	
	public String getAgents() throws RmesException;
	
	public void setAddRole(String role, String user);
	
	public void setDeleteRole(String roles, String user);

	public String checkSugoiConnexion() throws RmesException;
	
}
