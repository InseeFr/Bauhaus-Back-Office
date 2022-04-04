package fr.insee.rmes.config.auth.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.HttpMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.Jwt;
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
				.antMatchers("/init","/stamps","/disseminationStatus","/roles","/agents").permitAll() //PublicResources
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
		if (config.isRequiresSsl())
			http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
		
		logger.info("OpenID authentication activated ");
	
	}
	
	
	@Bean
	public UserProvider getUserProvider() {
		return auth -> {
			final Jwt jwt = (Jwt) auth.getPrincipal();
			Map<String,Object> claims = jwt.getClaims();
			logger.debug(claims.get(config.getRoleclaim()).toString());
			JsonParser parser = JsonParserFactory.getJsonParser();
			Map<String, Object> listeRoles = parser.parseMap(claims.get(config.getRoleclaim()).toString());
			List<String> roles = Arrays.asList(
								listeRoles.get("roles").toString()
									.substring(1,listeRoles.get("roles").toString().length() - 1) //remove []
									.replace(" ", "") //remove all spaces
									.split(",")
							);
		
//TODO	change way to have roles 	 
//			List<String> roles2 =
//			 auth.getAuthorities().stream()
//             .map(GrantedAuthority::getAuthority)
//             .map(String::toUpperCase)
//             .collect(Collectors.toList());
			return new User(roles, jwt.getClaimAsString(config.getStampclaim()));
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
	
}
