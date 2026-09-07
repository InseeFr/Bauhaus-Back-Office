package fr.insee.rmes.modules.commons.configuration.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = OpenApiConfiguration.ENABLED_PROPERTY, havingValue = "true")
public class OpenApiConfiguration {

    public static final String ENABLED_PROPERTY = "fr.insee.rmes.bauhaus.swagger.enabled";

    public static final String TITLE = "Bauhaus";
    public static final String DESCRIPTION = "Back office de Bauhaus (rmesgncs)";

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    /**
     * Chemins servis par springdoc. Le navigateur qui charge l'UI ne présente aucun jeton : sans
     * ouverture explicite, la chaîne applicative ({@code anyRequest().authenticated()}) rendrait la
     * documentation inaccessible.
     */
    private static final String[] SWAGGER_PATHS = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

    @Bean
    public OpenAPI openAPI(@Value("${fr.insee.rmes.bauhaus.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .version(appVersion)
                        .description(DESCRIPTION))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // Bouton « Authorize » de l'UI, appliqué à toutes les opérations.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));
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
