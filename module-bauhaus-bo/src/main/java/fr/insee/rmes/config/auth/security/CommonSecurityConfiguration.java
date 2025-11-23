package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

import static fr.insee.rmes.PropertiesKeys.CORS_ALLOWED_ORIGIN;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class CommonSecurityConfiguration {

    private static final Logger logger= LoggerFactory.getLogger(CommonSecurityConfiguration.class);

    public static final String DEFAULT_ROLE_PREFIX = "" ;
    private final Optional<String> allowedOrigin ;
    private final String appHost;

    public CommonSecurityConfiguration(
            @Value("${"+CORS_ALLOWED_ORIGIN+"}") Optional<String> allowedOrigin,
            @Value("${fr.insee.rmes.bauhaus.appHost}") String appHost) {
        this.allowedOrigin=allowedOrigin;
        this.appHost = appHost;
    }

    @Bean
    static AnnotationTemplateExpressionDefaults templateExpressionDefaults() {
        return new AnnotationTemplateExpressionDefaults();
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


    @Bean
    public StampFromPrincipal stampFromPrincipal(UserDecoder userDecoder){
        return principal -> {
            try {
                return userDecoder.fromPrincipal(principal).map(User::stamp);
            } catch (MissingUserInformationException _) {
                return Optional.empty();
            }
        };
    }
}
