package fr.insee.rmes.config.roles;

public interface UserRolesManagerService {
	
	public String getAuth(String body);
	
	public String getRoles();
	
	public String getAgents();
	
	public void setAddRole(String body);
	
	public void setDeleteRole(String body);
	
}
