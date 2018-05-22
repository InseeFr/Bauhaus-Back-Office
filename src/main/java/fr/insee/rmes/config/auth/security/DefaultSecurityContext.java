package fr.insee.rmes.config.auth.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
import fr.insee.rmes.config.Config;

public class DefaultSecurityContext extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		if (Config.REQUIRES_SSL) {
			http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure().and().csrf().disable();
		} else {
			http.csrf().disable();
		}
	}

}