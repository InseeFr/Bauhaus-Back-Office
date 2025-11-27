package fr.insee.rmes.modules.users;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.modules.users.domain.DomainAccessPrivilegesChecker;
import fr.insee.rmes.modules.users.domain.DomainUserService;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.modules.users.infrastructure.DevAuthenticationFilter;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.empty;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class UserConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(UserConfiguration.class);
    public static final String[] PUBLIC_RESOURCES_ANT_PATTERNS = {"/init", "/disseminationStatus"};

    private final boolean requireSsl;
    private final Optional<String> allowedOrigin;
    private final String appHost;
    private final JwtProperties jwtProperties;
    private final String env;

    public UserConfiguration(
            @Value("${fr.insee.rmes.bauhaus.force.ssl}")boolean requireSsl,
            @Value("${fr.insee.rmes.bauhaus.cors.allowedOrigin}") Optional<String> allowedOrigin,
            @Value("${fr.insee.rmes.bauhaus.appHost}") String appHost,
            JwtProperties jwtProperties,
            @Value("${fr.insee.rmes.bauhaus.env}") String env) {
        this.requireSsl = requireSsl;
        this.allowedOrigin = allowedOrigin;
        this.appHost = appHost;
        this.jwtProperties = jwtProperties;
        this.env = env;
    }



    @Bean
    static AnnotationTemplateExpressionDefaults templateExpressionDefaults() {
        return new AnnotationTemplateExpressionDefaults();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(this::extractAuthoritiesFromJwt);
        jwtAuthenticationConverter.setPrincipalClaimName(jwtProperties.getIdClaim());
        return jwtAuthenticationConverter;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean isProd = "PROD".equalsIgnoreCase(env);

        http.sessionManagement(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        if (!isProd) {
            http.addFilterBefore(new DevAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        }

        http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(withDefaults()))
                .authorizeHttpRequests(
                        authorizeHttpRequest -> authorizeHttpRequest
                                .requestMatchers(PUBLIC_RESOURCES_ANT_PATTERNS).permitAll()
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

        if (requireSsl) {
            http.requiresChannel(channel -> channel.requestMatchers("/**").requiresSecure());
        }

        logger.info(isProd ? "OpenID authentication activated" : "Development mode with FAKE_USER");

        return http.build();
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


    private interface RoleClaim{
        ArrayOfRoles arrayOfRoles();
    }

    private interface ArrayOfRoles{
        Stream<String> stream();
    }

    @Bean
    public UserDecoder getProdUserProvider(JwtProperties jwtProperties) {
        return new OidcUserDecoder(jwtProperties);
    }

    @Bean
    UserService userService(UserDecoder decoder, RbacFetcher rbacFetcher){
        return new DomainUserService(decoder, rbacFetcher);
    }

    @Bean(value = "propertiesAccessPrivilegesChecker")
    AccessPrivilegesCheckerService accessPrivilegesChecker(
            RbacFetcher rbacFetcher,
            UserDecoder userDecoder,
            StampChecker infrastructureStampChecker
    ){
        return  new DomainAccessPrivilegesChecker(rbacFetcher, userDecoder, infrastructureStampChecker);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                logger.info("Allowed origins : {}", allowedOrigin);
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigin.orElse(appHost))
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }

        };
    }
}
