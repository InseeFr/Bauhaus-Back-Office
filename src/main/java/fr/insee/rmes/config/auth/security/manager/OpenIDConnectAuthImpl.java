package fr.insee.rmes.config.auth.security.manager;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.auth.conditions.OpenIDConnectAuthCondition;

@Service
@Conditional(value = OpenIDConnectAuthCondition.class)
public class OpenIDConnectAuthImpl implements SecurityManagerService {
	
	@Override
	public String getAuthType() {
		return AuthType.OPEN_ID_CONNECT_AUTH.getAuth();
	}
	
	/**
	 * OpenIDConnectAuthImpl 
	 * 
	 * No need for this auth type 
	 */

	public User postAuth(String body) {
		return null;
	}
	

}
