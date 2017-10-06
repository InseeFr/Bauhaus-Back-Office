package fr.insee.rmes.persistance.securityManager;

public interface SecurityManagerContract {
	
	public String getRoles();
	
	public String getAgents();
	
	public void setAddRole(String body);
	
	public void setDeleteRole(String body);
	
}
