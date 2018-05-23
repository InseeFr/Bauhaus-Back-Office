package fr.insee.rmes.config.auth.security.manager;

public interface SecurityManagerService {
	
	public String getAuthType();
	
	public User postAuth(String body);
	
}
