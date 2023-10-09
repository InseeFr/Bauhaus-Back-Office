package fr.insee.rmes.config.auth.security;

import com.nimbusds.jose.shaded.json.JSONObject;
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
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.*;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true, prePostEnabled = true)
@ConditionalOnExpression("'PROD'.equalsIgnoreCase('${fr.insee.rmes.bauhaus.env}')")
public class OpenIDConnectSecurityContext{

	private static final Logger logger = LoggerFactory.getLogger(OpenIDConnectSecurityContext.class);

	public static final String TIMBRE_ANONYME = "bauhausGuest_STAMP";
	private static final List<String> EMPTY_ROLES = List.of();
	public static final String LOG_INFO_DEFAULT_STAMP = "User {} uses default stamp";

	private final String stampClaim;

	private final String roleClaim;

	private final String idClaim;

	private final boolean requiresSsl;
	private final String keyForRolesInRoleClaim;

	public OpenIDConnectSecurityContext(@Value("${fr.insee.rmes.bauhaus.cors.allowedOrigin}") Optional<String> allowedOrigin, @Value("${jwt.stamp-claim}") String stampClaim, @Value("${jwt.role-claim}") String roleClaim, @Value("${jwt.id-claim}") String idClaim, @Value("${fr.insee.rmes.bauhaus.force.ssl}") boolean requiresSsl, @Value("${jwt.role-claim.roles}") String keyForRolesInRoleClaim) {
		this.stampClaim = stampClaim;
		this.roleClaim = roleClaim;
		this.idClaim = idClaim;
		this.requiresSsl = requiresSsl;
		this.keyForRolesInRoleClaim = keyForRolesInRoleClaim;
	}

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
			var principal=auth.getPrincipal();
			Optional<Map<String, Object>> claims = "anonymousUser".equals(auth.getPrincipal()) ? empty() : of(((Jwt)principal).getClaims());
			return buildUserFromToken(claims);
		};
	}

	public User buildUserFromToken(Optional<Map<String, Object>> claims) {
		if (claims.isEmpty()) {
			return null;
		}
		var userClaims = claims.get();
		var id = (String) userClaims.get(idClaim);
		var stamp=ofNullable((String)userClaims.get(stampClaim));
		if (stamp.isEmpty()){
			logger.info(LOG_INFO_DEFAULT_STAMP, id);
			stamp= of(TIMBRE_ANONYME);
		}
		var objectForRoles = (JSONObject) userClaims.get(roleClaim);
		var roles = objectForRoles == null ? EMPTY_ROLES : ((List<String>) (objectForRoles.get(keyForRolesInRoleClaim)));

		logger.debug("Current User is {}, {} with roles {}", id, stamp, roles);
		return new User(id, roles == null ? EMPTY_ROLES : roles, stamp.get());
	}

	
}
