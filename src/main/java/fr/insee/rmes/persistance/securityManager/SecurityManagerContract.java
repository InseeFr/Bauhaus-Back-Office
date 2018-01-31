package fr.insee.rmes.persistance.securityManager;

public interface SecurityManagerContract {
	
	public String getAuth(String body);
	
	public String getRoles();
	
	public String getAgents();
	
	public void setAddRole(String body);
	
	public void setDeleteRole(String body);
	
}
