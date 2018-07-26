package fr.insee.rmes.config.auth.security.manager;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.auth.conditions.NoAuthCondition;
import fr.insee.rmes.config.roles.Constants;

@Service
@Conditional(value = NoAuthCondition.class)
public class NoAuthImpl implements SecurityManagerService {

	final static Logger logger = LogManager.getLogger(NoAuthImpl.class);
	
	@Override
	public String getAuthType() {
		return AuthType.NO_AUTH.getAuth();
	}
	
	/**
	 * NoAuthImpl 
	 * @param body : empty
	 * 
	 * @return Admin role
	 */
	
	public User postAuth(String body) {
		return new User(new JSONArray(Arrays.asList(Constants.ADMIN)), "XXXXXX");
	}
	
}
