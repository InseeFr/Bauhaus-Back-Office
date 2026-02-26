package fr.insee.rmes.modules.users;

import fr.insee.rmes.modules.users.domain.DomainAccessPrivilegesChecker;
import fr.insee.rmes.modules.users.domain.DomainUserService;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.infrastructure.DevAuthenticationFilter;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.RoleClaimExtractor;
import fr.insee.rmes.BauhausConfiguration;
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

import java.util.Collection;
import java.util.Optional;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class UserConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(UserConfiguration.class);
    public static final String[] PUBLIC_RESOURCES_ANT_PATTERNS = {"/init", "/disseminationStatus"};

    private final boolean requireSsl;
    private final Optional<String> allowedOrigin;
    private final BauhausConfiguration bauhausConfiguration;
    private final JwtProperties jwtProperties;
    private final RoleClaimExtractor roleClaimExtractor;

    public UserConfiguration(
            @Value("${fr.insee.rmes.bauhaus.force.ssl}") boolean requireSsl,
            @Value("${fr.insee.rmes.bauhaus.cors.allowedOrigin}") Optional<String> allowedOrigin,
            BauhausConfiguration bauhausConfiguration,
            JwtProperties jwtProperties,
            RoleClaimExtractor roleClaimExtractor) {
        this.requireSsl = requireSsl;
        this.allowedOrigin = allowedOrigin;
        this.bauhausConfiguration = bauhausConfiguration;
        this.jwtProperties = jwtProperties;
        this.roleClaimExtractor = roleClaimExtractor;
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
        boolean isProd = "PROD".equalsIgnoreCase(bauhausConfiguration.env());

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
        return roleClaimExtractor.extractRoles(jwt.getClaims()).map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast).toList();
    }

    @Bean
    public UserDecoder getProdUserProvider(OrganisationsService organisationsService, JwtProperties jwtProperties, RoleClaimExtractor roleClaimExtractor) {
        return new OidcUserDecoder(organisationsService, jwtProperties, roleClaimExtractor);
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
                        .allowedOrigins(allowedOrigin.orElse(bauhausConfiguration.appHost()))
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }

        };
    }
}
