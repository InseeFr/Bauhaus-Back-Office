package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

import static fr.insee.rmes.config.PropertiesKeys.CORS_ALLOWED_ORIGIN;

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
            } catch (RmesException e) {
                return Optional.empty();
            }
        };
    }
}
