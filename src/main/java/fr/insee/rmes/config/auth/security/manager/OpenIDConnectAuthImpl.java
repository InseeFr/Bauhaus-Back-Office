package fr.insee.rmes.config.auth.security.manager;

import java.util.Arrays;

import org.json.JSONArray;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.auth.conditions.OpenIDConnectAuthCondition;
import fr.insee.rmes.config.roles.Role;

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
		return new User(new JSONArray(Arrays.asList(Role.ADMIN.getRole())), "XXXXXX");
	}
	

}
