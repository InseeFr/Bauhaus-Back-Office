package fr.insee.rmes.config.auth.security;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.conditions.BasicAuthCondition;
import fr.insee.rmes.config.auth.conditions.NoAuthCondition;

@Configuration
@Conditional(value = {BasicAuthCondition.class, NoAuthCondition.class})
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