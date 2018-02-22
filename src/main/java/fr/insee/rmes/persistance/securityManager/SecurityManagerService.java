package fr.insee.rmes.persistance.securityManager;

public interface SecurityManagerService {
	
	public String getAuthType();
	
	public User postAuth(String body);
	
}
