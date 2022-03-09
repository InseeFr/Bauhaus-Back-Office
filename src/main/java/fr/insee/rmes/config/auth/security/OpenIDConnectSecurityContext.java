package fr.insee.rmes.config.auth.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import javax.ws.rs.HttpMethod;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
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
import fr.insee.rmes.config.auth.security.conditions.OpenIDConnectAuthCondition;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.config.auth.user.UserProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true, prePostEnabled = true)
@Conditional(value = OpenIDConnectAuthCondition.class)
public class OpenIDConnectSecurityContext extends WebSecurityConfigurerAdapter  {
	
//	private static final Logger log = LoggerFactory.getLogger(OpenIDConnectSecurityContext.class);
	


//	@Autowired
//	RmesAuthenticationEntryPoint entryPoint;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().disable();

		http.cors(withDefaults())
				.authorizeRequests()
				.antMatchers("/api/init").permitAll()
				.antMatchers("/api/healthcheck").permitAll()
				.antMatchers("/swagger-ui/*").permitAll()
				.antMatchers("/api/openapi.json").permitAll()
				.antMatchers("/api/documents/document/*/file").permitAll()
				.antMatchers("/api/operations/operation/codebook").permitAll()
				.antMatchers(HttpMethod.OPTIONS).permitAll()
				.anyRequest().authenticated()
				.and()
				.oauth2ResourceServer()
	//				.authenticationEntryPoint(entryPoint )
				.jwt()
				.jwkSetUri("https://auth.insee.test/auth/realms/agents-insee-interne");
		if (Config.REQUIRES_SSL)
			http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
	
	}
	
	
	@Bean
	public UserProvider getUserProvider() {
		return auth -> {
			final Jwt jwt = (Jwt) auth.getPrincipal();
			return new User(jwt.getClaimAsStringList(Config.roleClaim), jwt.getClaimAsString(Config.stampClaim));
		};
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		//configuration.setAllowedOrigins(allowedOrigin.stream().collect(Collectors.toList()));
		configuration.setAllowedMethods(List.of("*"));
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
