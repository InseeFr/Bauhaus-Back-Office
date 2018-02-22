package fr.insee.rmes.persistance.securityManager;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.conditions.FakeAuthCondition;
import fr.insee.rmes.persistance.userRolesManager.Role;


@Service
@Conditional(value = FakeAuthCondition.class)
public class FakeAuthImpl implements SecurityManagerService {

	final static Logger logger = LogManager.getLogger(FakeAuthImpl.class);
	
	@Override
	public String getAuthType() {
		return AuthType.FAKE_AUTH.getAuth();
	}
	
	/**
	 * FakeAuthImpl 
	 * @param body : password
	 * 
	 * @return Full functionality role or read only
	 */
	
	public User postAuth(String body) {
		User user = new User();
		user.setStamp("XXXXXX");
		if (body.equals(Config.PASSWORD_GESTIONNAIRE)) user.setRole(Role.ADMIN);
		else if (body.equals(Config.PASSWORD_PRODUCTEUR)) user.setRole(Role.GUEST); 
		return user;
	}

}
