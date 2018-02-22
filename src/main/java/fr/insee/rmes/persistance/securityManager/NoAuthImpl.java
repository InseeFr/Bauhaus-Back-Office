package fr.insee.rmes.persistance.securityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.auth.conditions.NoAuthCondition;
import fr.insee.rmes.persistance.userRolesManager.Role;

@Service
@Conditional(value = NoAuthCondition.class)
public class NoAuthImpl implements SecurityManagerService {

	final static Logger logger = LogManager.getLogger(NoAuthImpl.class);
	
	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return AuthType.NO_AUTH.getAuth();
	}

	public String getAuth(String body) {
		return Role.ADMIN.getRole();
	}
	
}
