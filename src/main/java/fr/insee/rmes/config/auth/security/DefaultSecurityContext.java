package fr.insee.rmes.config.auth.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import fr.insee.rmes.config.Config;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=false, prePostEnabled = true)
@ConditionalOnMissingBean(OpenIDConnectSecurityContext.class)
public class DefaultSecurityContext extends WebSecurityConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(DefaultSecurityContext.class);

	
	@Value("${fr.insee.rmes.bauhaus.cors.allowedOrigin}")
	private Optional<String> allowedOrigin;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.cors(withDefaults())
		.authorizeRequests().anyRequest().permitAll();
		if (Config.REQUIRES_SSL) {
			http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
		}
		
		logger.info("Default authentication activated - no auth ");
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		logger.info("Allowed origins : {}", allowedOrigin);
		configuration.setAllowedOrigins(List.of(allowedOrigin.get()));
		configuration.setAllowedMethods(List.of("*"));
		configuration.setAllowedHeaders(List.of("*"));
		UrlBasedCorsConfigurationSource source = new
				UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}