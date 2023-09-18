package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.config.auth.user.UserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true, prePostEnabled = true)
@ConditionalOnExpression("'PROD'.equalsIgnoreCase('${fr.insee.rmes.bauhaus.env}')")
public class OpenIDConnectSecurityContext{

	private static final Logger logger = LoggerFactory.getLogger(OpenIDConnectSecurityContext.class);

	@Value("${jwt.stamp-claim}")
	private String stampClaim;

	@Value("${jwt.id-claim}")
	private String idClaim;

	@Value("${jwt.role-claim}")
	private String roleClaim;

	@Value("${fr.insee.rmes.bauhaus.force.ssl}")
	private boolean requiresSsl = false;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
		if (requiresSsl){
			http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
		}

		logger.info("OpenID authentication activated ");

		return http.build();

	}


	@Bean
	public UserProvider getUserProvider() {
		return auth -> {
			if ("anonymousUser".equals(auth.getPrincipal())) return null; //init request, or request without authentication
			final Jwt jwt = (Jwt) auth.getPrincipal();
			Map<String,Object> claims = jwt.getClaims();
			JsonParser parser = JsonParserFactory.getJsonParser();
			Map<String, Object> listeRoles = parser.parseMap(claims.get(roleClaim).toString());
			List<String> roles = Arrays.asList(
								listeRoles.get("roles").toString()
									.substring(1,listeRoles.get("roles").toString().length() - 1) //remove []
									.replace(" ", "") //remove all spaces
									.split(",")
							);
			String stamp = claims.get(stampClaim).toString();
			String id = claims.get(idClaim).toString();
			logger.debug("Current User is {}, {} with roles {}",id,stamp,roles);
			return new User(id,roles, stamp);
		};
	}
	
}
