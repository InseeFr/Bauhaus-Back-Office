package fr.insee.rmes.config.auth.security;

import com.nimbusds.jose.shaded.json.JSONObject;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fr.insee.rmes.config.auth.security.CommonSecurityConfiguration.DEFAULT_ROLE_PREFIX;
import static java.util.Optional.*;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@ConditionalOnExpression("'PROD'.equalsIgnoreCase('${fr.insee.rmes.bauhaus.env}')")
public class OpenIDConnectSecurityContext {

    private static final Logger logger = LoggerFactory.getLogger(OpenIDConnectSecurityContext.class);

    public static final String TIMBRE_ANONYME = "bauhausGuest_STAMP";
    private static final Stream<String> EMPTY_ROLES = Stream.empty();
    public static final String LOG_INFO_DEFAULT_STAMP = "User {} uses default stamp";
    public static final String[] PUBLIC_RESOURCES_ANT_PATTERNS = {"/init", "/stamps", "/disseminationStatus", "/roles"};

    private final String stampClaim;

    private final String roleClaim;

    private final String idClaim;

    private final boolean requiresSsl;
    private final String keyForRolesInRoleClaim;

    public OpenIDConnectSecurityContext(@Value("${jwt.stamp-claim}") String stampClaim, @Value("${jwt.role-claim}") String roleClaim, @Value("${jwt.id-claim}") String idClaim, @Value("${fr.insee.rmes.bauhaus.force.ssl}") boolean requiresSsl, @Value("${jwt.role-claim.roles}") String keyForRolesInRoleClaim) {
        this.stampClaim = stampClaim;
        this.roleClaim = roleClaim;
        this.idClaim = idClaim;
        this.requiresSsl = requiresSsl;
        this.keyForRolesInRoleClaim = keyForRolesInRoleClaim;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement().disable()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests()
                .antMatchers(PUBLIC_RESOURCES_ANT_PATTERNS).permitAll() //PublicResources
                .antMatchers("/healthcheck").permitAll()
                .antMatchers("/swagger-ui/*").permitAll()
                .antMatchers("/v3/api-docs/swagger-config", "/v3/api-docs").permitAll()
                .antMatchers("/openapi.json").permitAll()
                .antMatchers("/documents/document/*/file").permitAll()
                .antMatchers("/operations/operation/codebook").permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated()
                .expressionHandler(webSecurityExpressionHandler());

        if (requiresSsl) {
            http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
        }
        logger.info("OpenID authentication activated ");

        return http.build();
    }

    private DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        var expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setDefaultRolePrefix(DEFAULT_ROLE_PREFIX);
        return expressionHandler;
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(this::extractAuthoritiesFromJwt);
        jwtAuthenticationConverter.setPrincipalClaimName(idClaim);
        return jwtAuthenticationConverter;
    }

    @Bean
    public UserDecoder userDecoder() {
        return principal -> "anonymousUser".equals(principal) ?
                empty() :
                of(buildUserFromToken(((Jwt) principal).getClaims()));
    }

    public User buildUserFromToken(Map<String, Object> claims) throws RmesException {
        if (claims.isEmpty()) {
            throw new RmesException(HttpStatus.UNAUTHORIZED, "Must be authentified", "empty claims for JWT");
        }
        var id = (String) claims.get(idClaim);
        var stamp = ofNullable((String) claims.get(stampClaim));
        if (stamp.isEmpty()) {
            logger.info(LOG_INFO_DEFAULT_STAMP, id);
            stamp = of(TIMBRE_ANONYME);
        }
        var roles=extractRoles(claims).toList();

        logger.debug("Current User is {}, {} with roles {}", id, stamp, roles);
        return new User(id, roles, stamp.get());
    }

    private Collection<GrantedAuthority> extractAuthoritiesFromJwt(Jwt jwt){
        return extractRoles(jwt.getClaims()).map(SimpleGrantedAuthority::new)
                .map(a->(GrantedAuthority)a).toList();
    }

    private Stream<String> extractRoles(Map<String, Object> claims){
        var objectForRoles = (JSONObject) claims.get(roleClaim);
        return objectForRoles == null ? EMPTY_ROLES : ((List<String>) (objectForRoles.get(keyForRolesInRoleClaim))).stream();
    }


}
