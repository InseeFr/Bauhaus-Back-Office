package fr.insee.rmes.config.auth.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.HttpMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.config.auth.user.UserProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true, prePostEnabled = true)
@ConditionalOnExpression("'PROD'.equals('${fr.insee.rmes.bauhaus.env}')")
public class OpenIDConnectSecurityContext extends WebSecurityConfigurerAdapter  {
	
	private static final Logger logger = LoggerFactory.getLogger(OpenIDConnectSecurityContext.class);
	
	@Value("${fr.insee.rmes.bauhaus.cors.allowedOrigin}")
	private Optional<String> allowedOrigin;
	
	@Autowired
	Config config;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().disable();

		http.cors(withDefaults())
				.authorizeRequests()
				.antMatchers("/init").permitAll()
				.antMatchers("/healthcheck").permitAll()
				.antMatchers("/swagger-ui/*").permitAll()
				.antMatchers("/v3/api-docs/swagger-config", "/v3/api-docs").permitAll()
				.antMatchers("/openapi.json").permitAll()
				.antMatchers("/documents/document/*/file").permitAll()
				.antMatchers("/operations/operation/codebook").permitAll()
				.antMatchers(HttpMethod.OPTIONS).permitAll()
				.anyRequest().authenticated()
				.and()
				.oauth2ResourceServer()
				.jwt();
				//.jwkSetUri("https://auth.insee.test/auth/realms/agents-insee-interne");
		if (config.isRequiresSsl())
			http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
		
		logger.info("OpenID authentication activated ");
	
	}
	
	
	@Bean
	public UserProvider getUserProvider() {
		return auth -> {
			final Jwt jwt = (Jwt) auth.getPrincipal();
			return new User(jwt.getClaimAsStringList(config.getRoleclaim()), jwt.getClaimAsString(config.getStampclaim()));
		};
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
	
	 @Bean
	    public NimbusJwtDecoder nimbusJwtDecoder(){
	        RestTemplate rest = new RestTemplate();
	        rest.getInterceptors().add((request, body, execution) -> 
	            execution.execute(request, body)
	        );
	        return NimbusJwtDecoder.withJwkSetUri("https://auth.insee.test/auth/realms/agents-insee-interne")
	                .restOperations(rest).build();
	    }

}
