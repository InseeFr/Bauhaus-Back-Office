package fr.insee.rmes.config.auth.security.keycloak;

import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import fr.insee.rmes.config.Config;

@Configuration
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
public class KeycloakSecurityContext extends KeycloakWebSecurityConfigurerAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(KeycloakSecurityContext.class);

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.csrf().disable();
		if (Config.REQUIRES_SSL) http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
		http.sessionManagement().disable();
		http.authorizeRequests()
			.antMatchers("/api/init", "/api/keycloak").permitAll()
			.anyRequest().authenticated();
	}

	/**
	 * Registers the KeycloakAuthenticationProvider with the authentication
	 * manager.
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}
	
	@Override
    @Bean
    public KeycloakUserDetailsAuthenticationProvider keycloakAuthenticationProvider() {
        log.info("adding keycloak authentication provider");
        return new KeycloakUserDetailsAuthenticationProvider();
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
