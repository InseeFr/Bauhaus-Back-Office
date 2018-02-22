package fr.insee.rmes.persistance.securityManager;

public interface SecurityManagerService {
	
	public String getAuthType();
	
	public String getAuth(String body);
	
}
