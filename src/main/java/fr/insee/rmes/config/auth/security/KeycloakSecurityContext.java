package fr.insee.rmes.config.auth.security;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.conditions.OpenIDConnectAuthCondition;
import fr.insee.rmes.config.auth.security.keycloak.KeycloakUserDetailsAuthenticationProvider;
import fr.insee.rmes.config.auth.security.keycloak.RmesKeycloakConfigResolver;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true, prePostEnabled = true)
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
@Conditional(value = OpenIDConnectAuthCondition.class)
public class KeycloakSecurityContext extends KeycloakWebSecurityConfigurerAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(KeycloakSecurityContext.class);

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.csrf().disable();
		if (Config.REQUIRES_SSL) http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
		http.sessionManagement().disable();
		http.authorizeRequests()
			.antMatchers("/api/init","api/agents","api/roles","api/stamps").permitAll()
			.antMatchers("/api/healthcheck").permitAll()
			.antMatchers("/swagger-ui/*").permitAll()
			.antMatchers("/api/openapi.json").permitAll()
			.antMatchers("/api/documents/document/*/file").permitAll()
			.antMatchers("/api/operations/operation/codebook").permitAll()
			.anyRequest().authenticated();
	}

	/**
	 * Registers the KeycloakAuthenticationProvider with the authentication
	 * manager.
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}
	
	/**
	 * Defines the keycloak authentication provider
	 */
	@Override
    @Bean
    public KeycloakUserDetailsAuthenticationProvider keycloakAuthenticationProvider() {
        log.info("adding keycloak authentication provider");
        return new KeycloakUserDetailsAuthenticationProvider();
    }
	
	/**
	 * Defines the keycloak configuration
	 */
    @Bean
	public KeycloakConfigResolver keycloakConfigResolver() {
		log.info("adding RMeS keycloak config resolver");
        return new RmesKeycloakConfigResolver();
	}

	/**
	 * Defines the session authentication strategy.
	 */
	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new NullAuthenticatedSessionStrategy();
	}

}
