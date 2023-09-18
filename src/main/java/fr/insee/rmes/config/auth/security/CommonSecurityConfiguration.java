package fr.insee.rmes.config.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

@Configuration
public class CommonSecurityConfiguration {

    private static final Logger logger= LoggerFactory.getLogger(CommonSecurityConfiguration.class);
    private final Optional<String> allowedOrigin ;

    public CommonSecurityConfiguration(@Value("${fr.insee.rmes.bauhaus.cors.allowedOrigin}") Optional<String> allowedOrigin){
        this.allowedOrigin=allowedOrigin;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        logger.info("Allowed origins : {}", allowedOrigin);
        configuration.setAllowedOrigins(List.of(allowedOrigin.orElse("*")));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new
                UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
