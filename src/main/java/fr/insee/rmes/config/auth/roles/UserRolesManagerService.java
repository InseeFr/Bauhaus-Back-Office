package fr.insee.rmes.config.auth.roles;

import fr.insee.rmes.exceptions.RmesException;

public interface UserRolesManagerService {
	
	public String getAuth(String body);
	
	public String getRoles() throws RmesException;
	
	public String getAgents() throws RmesException;
	
	public void setAddRole(String body);
	
	public void setDeleteRole(String body);
	
}
