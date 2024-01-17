package fr.insee.rmes.config.auth.security;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
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
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    public static final String[] PUBLIC_RESOURCES_ANT_PATTERNS = {"/init", "/stamps", "/disseminationStatus"};

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
        http.sessionManagement(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(withDefaults()))
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorizeHttpRequest -> authorizeHttpRequest
                                .requestMatchers(PUBLIC_RESOURCES_ANT_PATTERNS).permitAll() //PublicResources
                                .requestMatchers("/healthcheck").permitAll()
                                .requestMatchers("/swagger-ui/*").permitAll()
                                .requestMatchers("/v3/api-docs/swagger-config", "/v3/api-docs").permitAll()
                                .requestMatchers("/openapi.json").permitAll()
                                .requestMatchers("/documents/document/*/file").permitAll()
                                .requestMatchers("/operations/operation/codebook").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                                .anyRequest().authenticated()
                );

        if (requiresSsl) {
            http.requiresChannel(channel->channel.requestMatchers("/**").requiresSecure());
        }
        logger.info("OpenID authentication activated ");

        return http.build();
    }

    @Bean
    static GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(DEFAULT_ROLE_PREFIX);
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
        var roles = extractRoles(claims).toList();

        logger.debug("Current User is {}, {} with roles {}", id, stamp, roles);
        return new User(id, roles, stamp.get());
    }

    private Collection<GrantedAuthority> extractAuthoritiesFromJwt(Jwt jwt) {
        return extractRoles(jwt.getClaims()).map(SimpleGrantedAuthority::new)
                .map(a -> (GrantedAuthority) a).toList();
    }

    private Stream<String> extractRoles(Map<String, Object> claims) {
        var objectForRoles = (JsonObject) claims.get(roleClaim);
        if (objectForRoles==null){
            return EMPTY_ROLES;
        }
        var jsonArray=(JsonArray) objectForRoles.get(keyForRolesInRoleClaim);
        return StreamSupport.stream(Spliterators.spliterator(jsonArray.iterator(), jsonArray.size(),0),false)
                        .map(JsonElement::getAsString);
    }

}
