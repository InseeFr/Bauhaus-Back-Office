package fr.insee.rmes.persistance.securityManager;

import org.json.JSONObject;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.auth.conditions.BasicAuthCondition;

@Service
@Conditional(value = BasicAuthCondition.class)
public class BasicAuthImpl implements SecurityManagerService {
	
	@Override
	public String getAuthType() {
		return AuthType.BASIC_AUTH.getAuth();
	}
	
	/**
	 * BasicAuthImpl 
	 * @param body : basic json with role and stamp key
	 * 
	 * @return All role and stamp combinations
	 */

	public User postAuth(String body) {
		JSONObject bodyJson = new JSONObject(body);
		User user = new User(bodyJson.getString("role"), bodyJson.getString("stamp"));
		return user;
	}

}
