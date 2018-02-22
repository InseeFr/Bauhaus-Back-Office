package fr.insee.rmes.persistance.securityManager;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.auth.conditions.BasicAuthCondition;
import fr.insee.rmes.persistance.userRolesManager.Role;

@Service
@Conditional(value = BasicAuthCondition.class)
public class BasicAuthImpl implements SecurityManagerService {
	
	@Override
	public String getAuthType() {
		return AuthType.BASIC_AUTH.getAuth();
	}

	public String getAuth(String body) {
		return Role.ADMIN.getRole();
	}

}
