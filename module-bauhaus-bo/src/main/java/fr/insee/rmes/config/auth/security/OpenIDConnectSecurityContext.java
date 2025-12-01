package fr.insee.rmes.config.auth.security;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import fr.insee.rmes.domain.auth.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.port.serverside.UserDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static fr.insee.rmes.config.auth.security.CommonSecurityConfiguration.DEFAULT_ROLE_PREFIX;
import static java.util.Optional.*;

import java.util.Optional;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@ConditionalOnExpression("'PROD'.equalsIgnoreCase('${fr.insee.rmes.bauhaus.env}')")
public class OpenIDConnectSecurityContext {

    private static final Logger logger = LoggerFactory.getLogger(OpenIDConnectSecurityContext.class);

    public static final String LOG_INFO_DEFAULT_STAMP = "User {} uses default stamp";
    public static final String[] PUBLIC_RESOURCES_ANT_PATTERNS = {"/init", "/disseminationStatus"};

    private final JwtProperties jwtProperties;
    private final boolean requiresSsl;

    public OpenIDConnectSecurityContext(
            JwtProperties jwtProperties,
            @Value("${fr.insee.rmes.bauhaus.force.ssl}") boolean requiresSsl) {
        this.jwtProperties = jwtProperties;
        this.requiresSsl = requiresSsl;
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
                                .requestMatchers("/v3/api-docs/swagger-config", "/v3/api-docs/**").permitAll()
                                .requestMatchers("/openapi.json").permitAll()
                                .requestMatchers("/documents/document/*/file").permitAll()
                                .requestMatchers("/operations/operation/codebook").permitAll()
                                .requestMatchers("/colectica/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                                .anyRequest().authenticated()
                );

        if (requiresSsl) {
            http.requiresChannel(channel -> channel.requestMatchers("/**").requiresSecure());
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
        jwtAuthenticationConverter.setPrincipalClaimName(jwtProperties.getIdClaim());
        return jwtAuthenticationConverter;
    }

    @Bean
    public UserDecoder userDecoder() {
        return principal -> "anonymousUser".equals(principal) ?
                empty() :
                of(buildUserFromToken(((Jwt) principal).getClaims()));
    }

    protected User buildUserFromToken(Map<String, Object> claims) throws RmesException {
        if (claims.isEmpty()) {
            throw new RmesException(HttpStatus.UNAUTHORIZED, "Must be authentified", "empty claims for JWT");
        }
        var id = (String) claims.get(jwtProperties.getIdClaim());
        var stamp = extractStamp(claims, id);

        var source = (String) claims.get(jwtProperties.getSourceClaim());
        var roles = extractRoles(claims).toList();

        logger.debug("Current User is {}, {} with roles {} from source {}", id, stamp, roles, source);

        if(stamp.isEmpty()){
            return new User(id, roles, null, source);
        }
        return new User(id, roles, stamp.get(), source);
    }

    private Collection<GrantedAuthority> extractAuthoritiesFromJwt(Jwt jwt) {
        return extractRoles(jwt.getClaims()).map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast).toList();
    }

    private Stream<String> extractRoles(Map<String, Object> claims) {
        RoleClaim roleClaim = roleClaimFrom(claims);
        ArrayOfRoles arrayOfRoles=roleClaim.arrayOfRoles();
        return arrayOfRoles.stream();
    }

    private RoleClaim roleClaimFrom(Map<String, Object> claims) {
        var valueForRoleClaim=switch (claims.get(jwtProperties.getRoleClaim())) {
            case JsonObject objectForRoles -> objectForRoles.getAsJsonArray(jwtProperties.getRoleClaimConfig().getRoles());
            case Map < ?, ?> mapForRoles -> mapForRoles.get(jwtProperties.getRoleClaimConfig().getRoles());
            default -> empty();
        };
        return roleClaimFrom(valueForRoleClaim);
    }

    private RoleClaim roleClaimFrom(Object listOrJsonArray) {
        return switch (listOrJsonArray){
            case JsonArray jsonArray -> () -> () -> jsonArrayToStream(jsonArray);
            case List<?> list -> () -> () -> list.stream().map(this::JsonElementOrElseToString);
            default -> () -> Stream::empty;
        };
    }

    private String JsonElementOrElseToString(Object element) {
        if (element instanceof JsonElement jsonElement){
            return jsonElement.getAsString();
        }
        return element.toString();
    }

    private Stream<String> jsonArrayToStream(JsonArray jsonArray) {
        return StreamSupport.stream(Spliterators.spliterator(jsonArray.iterator(), jsonArray.size(), 0), false)
                .map(JsonElement::getAsString);
    }

    private Optional<String> extractStamp(Map<String, Object> claims, String userId) {
        logger.debug("Extracting stamp for user {}", userId);
        
        var stamp = ofNullable((String) claims.get(jwtProperties.getStampClaim()));

        if (stamp.isPresent()) {
            logger.debug("Found stamp in stampClaim '{}' for user {}: {}", jwtProperties.getStampClaim(), userId, stamp.get());
            return stamp;
        } else {
            logger.debug("No stamp found in stampClaim '{}' for user {}, checking inseeGroupClaim", jwtProperties.getStampClaim(), userId);
            
            var inseeGroups = claims.get(jwtProperties.getInseeGroupClaim());
            stamp = extractStampFromInseeGroups(inseeGroups);
            
            if (stamp.isPresent()) {
                logger.debug("Found stamp in inseeGroupClaim '{}' for user {}: {}", jwtProperties.getInseeGroupClaim(), userId, stamp.get());
                return stamp;
            } else {
                logger.debug("No stamp found in inseeGroupClaim '{}' for user {}, using anonymous stamp", jwtProperties.getInseeGroupClaim(), userId);
                logger.info(LOG_INFO_DEFAULT_STAMP, userId);
                return empty();
            }
        }
    }

    private Optional<String> extractStampFromInseeGroups(Object inseeGroups) {
        if (inseeGroups == null) {
            return empty();
        }
        
        String suffix = "_" + jwtProperties.getHieApplicationPrefix();
        
        return switch (inseeGroups) {
            case List<?> list -> list.stream()
                    .map(this::JsonElementOrElseToString)
                    .filter(group -> group.endsWith(suffix))
                    .findFirst();
            default -> empty();
        };
    }

    private interface RoleClaim{
        ArrayOfRoles arrayOfRoles();
    }

    private interface ArrayOfRoles{
        Stream<String> stream();
    }
}
