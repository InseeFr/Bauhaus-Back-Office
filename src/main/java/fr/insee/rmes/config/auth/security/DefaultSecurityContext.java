package fr.insee.rmes.config.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import fr.insee.rmes.config.Config;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=false, prePostEnabled = true)
@ConditionalOnMissingBean(OpenIDConnectSecurityContext.class)
public class DefaultSecurityContext extends WebSecurityConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(DefaultSecurityContext.class);

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.authorizeRequests().anyRequest().permitAll();
		if (Config.REQUIRES_SSL) {
			http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
		}
		
		logger.debug("Default authentication activated - no auth ");
	}

}