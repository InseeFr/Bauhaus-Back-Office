package fr.insee.rmes.config.auth.roles;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;

import fr.insee.rmes.exceptions.RmesException;

public interface UserRolesManagerService {
	
	public String checkLdapConnexion()  throws RmesException ;
	
	public String getAuth(String body);
	
	public String getRoles() throws RmesException;
	
	public String getAgents() throws RmesException;
	
	public void setAddRole(String role, String user) throws AuthenticationException, ClientProtocolException, IOException ;
	
	public void setDeleteRole(String roles, String user) throws AuthenticationException, ClientProtocolException, IOException;

	public String checkSugoiConnexion() throws RmesException;
	
}
