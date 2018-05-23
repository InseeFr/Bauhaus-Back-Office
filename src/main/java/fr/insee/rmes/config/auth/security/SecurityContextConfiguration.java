package fr.insee.rmes.config.auth.security;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import fr.insee.rmes.config.auth.conditions.BasicAuthCondition;
import fr.insee.rmes.config.auth.conditions.NoAuthCondition;
import fr.insee.rmes.config.auth.conditions.OpenIDConnectAuthCondition;
import fr.insee.rmes.config.auth.security.keycloak.KeycloakSecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityContextConfiguration {
	
	
	@Conditional(value = OpenIDConnectAuthCondition.class)
	public KeycloakSecurityContext getKeycloakSecurityContext() {
		return new KeycloakSecurityContext();
	}
	
	@Conditional(value = {BasicAuthCondition.class, NoAuthCondition.class})
	public DefaultSecurityContext getSecurityContext() {
		return new DefaultSecurityContext();
	}

}
