package fr.insee.rmes.modules.commons.configuration.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Documentation OpenAPI et Swagger UI, activées par la propriété
 * {@value #ENABLED_PROPERTY} (désactivées par défaut, cf. application.properties qui la relaie
 * aux propriétés {@code springdoc.*}).
 * <p>
 * L'UI est servie sur {@code <contextPath>/swagger-ui.html}, le document sur
 * {@code <contextPath>/v3/api-docs}.
 * <p>
 * Deux façons de s'authentifier y sont décrites : {@value #BEARER_SCHEME_NAME}, où l'on colle un
 * jeton obtenu par ailleurs, et {@value #OAUTH_SCHEME_NAME}, où l'UI va elle-même chercher le jeton
 * auprès de Keycloak. Le flow OAuth2 vise <em>l'émetteur qui protège déjà l'API</em>
 * ({@code spring.security.oauth2.resourceserver.jwt.issuer-uri}) : le jeton obtenu depuis l'UI porte
 * donc les mêmes rôles, et ouvre exactement les mêmes droits, qu'un appel venu du front.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = OpenApiConfiguration.ENABLED_PROPERTY, havingValue = "true")
public class OpenApiConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OpenApiConfiguration.class);

    public static final String ENABLED_PROPERTY = "fr.insee.rmes.bauhaus.swagger.enabled";

    public static final String TITLE = "Bauhaus";
    public static final String DESCRIPTION = "Back office de Bauhaus (rmesgncs)";

    private static final String BEARER_SCHEME_NAME = "bearerAuth";
    private static final String OAUTH_SCHEME_NAME = "oauth2";

    private static final String ISSUER_PROPERTY = "spring.security.oauth2.resourceserver.jwt.issuer-uri";
    private static final String CLIENT_ID_PROPERTY = "fr.insee.rmes.bauhaus.swagger.oauth.client-id";

    /** Endpoints OIDC, relatifs à l'issuer du realm Keycloak. */
    private static final String AUTHORIZATION_ENDPOINT = "/protocol/openid-connect/auth";
    private static final String TOKEN_ENDPOINT = "/protocol/openid-connect/token";

    /** Portée demandée par le front (cf. {@code createReactOidc}), reprise à l'identique. */
    private static final String OPENID_SCOPE = "openid";

    /** Page servie par springdoc qui reçoit le code d'autorisation renvoyé par Keycloak. */
    private static final String OAUTH_REDIRECT_PATH = "swagger-ui/oauth2-redirect.html";

    /**
     * Chemins servis par springdoc. Le navigateur qui charge l'UI ne présente aucun jeton : sans
     * ouverture explicite, la chaîne applicative ({@code anyRequest().authenticated()}) rendrait la
     * documentation inaccessible. {@code /swagger-ui/**} couvre aussi
     * {@value #OAUTH_REDIRECT_PATH}, sur lequel Keycloak renvoie le navigateur.
     */
    private static final String[] SWAGGER_PATHS = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

    private final String appVersion;
    private final String issuerUri;
    private final String clientId;
    private final String contextPath;

    public OpenApiConfiguration(
            @Value("${fr.insee.rmes.bauhaus.version}") String appVersion,
            @Value("${" + ISSUER_PROPERTY + ":}") String issuerUri,
            @Value("${" + CLIENT_ID_PROPERTY + ":}") String clientId,
            @Value("${server.servlet.contextPath:/}") String contextPath) {
        this.appVersion = appVersion;
        // L'issuer est concaténé aux endpoints OIDC : un « / » final produirait des URLs à double slash,
        // que Keycloak refuse.
        this.issuerUri = trimTrailingSlash(issuerUri.trim());
        this.clientId = clientId.trim();
        this.contextPath = contextPath.endsWith("/") ? contextPath : contextPath + "/";
    }

    @Bean
    public OpenAPI openAPI() {
        Components components = new Components()
                .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .version(appVersion)
                        .description(DESCRIPTION))
                .components(components)
                // Bouton « Authorize » de l'UI, appliqué à toutes les opérations.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));

        if (oauthFlowDescribed()) {
            components.addSecuritySchemes(OAUTH_SCHEME_NAME, keycloakSecurityScheme());
            openAPI.addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME));
        }
        return openAPI;
    }

    /**
     * Flow « authorization code » : l'UI ouvre Keycloak, récupère le code sur
     * {@value #OAUTH_REDIRECT_PATH}, l'échange contre un jeton et le pose en en-tête
     * {@code Authorization}. Le rafraîchissement passe par le même endpoint que l'émission.
     */
    private SecurityScheme keycloakSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .in(SecurityScheme.In.HEADER)
                .description("Keycloak — même realm que celui qui valide les jetons de l'API (" + issuerUri + ")")
                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                        .authorizationUrl(authorizationUrl())
                        .tokenUrl(tokenUrl())
                        .refreshUrl(tokenUrl())
                        .scopes(new Scopes().addString(OPENID_SCOPE, "Jeton OIDC, comme celui du front"))));
    }

    /**
     * Trace, au démarrage, tout ce qu'il faut pour diagnostiquer un « Authorize » qui échoue :
     * URLs appelées, client_id envoyé, et redirect URI à déclarer côté Keycloak.
     */
    @PostConstruct
    void logOAuthConfiguration() {
        log.info("Swagger UI activé : documentation sur {}v3/api-docs, UI sur {}swagger-ui.html",
                contextPath, contextPath);

        if (!oauthFlowDescribed()) {
            log.warn("Swagger UI : pas de flow OAuth2 ({} non renseigné), le bouton « Authorize » n'offrira "
                    + "que le collage manuel d'un jeton", ISSUER_PROPERTY);
            return;
        }

        log.info("Swagger UI : jeton demandé à l'émetteur qui protège l'API ({}), donc mêmes rôles et mêmes "
                + "droits qu'un appel du front", issuerUri);
        log.info("Swagger UI : autorisation {} | jeton {} | client_id « {} » | scope « {} »",
                authorizationUrl(), tokenUrl(), clientId.isEmpty() ? "<absent>" : clientId, OPENID_SCOPE);
        log.info("Swagger UI : le client Keycloak doit avoir le flow standard activé et autoriser la redirect "
                + "URI <origine>{}{} (PKCE S256), sans quoi Keycloak répondra « Invalid parameter: redirect_uri »",
                contextPath, OAUTH_REDIRECT_PATH);

        if (clientId.isEmpty()) {
            log.warn("Swagger UI : aucun client_id ({} non renseigné), la fenêtre « Authorize » s'ouvrira avec "
                    + "un champ client_id vide, à saisir à la main", CLIENT_ID_PROPERTY);
        }
    }

    private boolean oauthFlowDescribed() {
        return !issuerUri.isEmpty();
    }

    private String authorizationUrl() {
        return issuerUri + AUTHORIZATION_ENDPOINT;
    }

    private String tokenUrl() {
        return issuerUri + TOKEN_ENDPOINT;
    }

    private static String trimTrailingSlash(String uri) {
        return uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(SWAGGER_PATHS)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request.anyRequest().permitAll())
                .build();
    }
}
