package fr.insee.rmes.persistance.securityManager;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.auth.conditions.OpenIDConnectAuthCondition;
import fr.insee.rmes.persistance.userRolesManager.Role;

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
	 * TODO 
	 */

	public User postAuth(String body) {
		User user = new User();
		user.setStamp("XXXXXX");
		user.setRole(Role.ADMIN);
		return user;
	}
	

}
