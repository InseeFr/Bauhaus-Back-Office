package fr.insee.rmes.config.auth.security;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.conditions.NoOpenIDConnectAuthCondition;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=false)
@Conditional(value = NoOpenIDConnectAuthCondition.class)
public class DefaultSecurityContext extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.authorizeRequests().anyRequest().permitAll();
		if (Config.REQUIRES_SSL) http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
	}

}