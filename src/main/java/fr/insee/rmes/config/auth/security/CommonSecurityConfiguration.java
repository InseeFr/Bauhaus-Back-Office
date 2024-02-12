package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

import static fr.insee.rmes.config.PropertiesKeys.CORS_ALLOWED_ORIGIN;

@Configuration
public class CommonSecurityConfiguration {

    private static final Logger logger= LoggerFactory.getLogger(CommonSecurityConfiguration.class);

    public static final String DEFAULT_ROLE_PREFIX = null ;
    private final Optional<String> allowedOrigin ;

    public CommonSecurityConfiguration(@Value("${"+CORS_ALLOWED_ORIGIN+"}") Optional<String> allowedOrigin){
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

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    public static class CustomGlobalMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {


        private final StampAuthorizationChecker stampAuthorizationChecker;
        private final StampFromPrincipal stampFromPrincipal;

        public CustomGlobalMethodSecurityConfiguration(StampAuthorizationChecker stampAuthorizationChecker, StampFromPrincipal stampFromPrincipal) {
            this.stampAuthorizationChecker = stampAuthorizationChecker;
            this.stampFromPrincipal = stampFromPrincipal;
        }

        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            logger.trace("Initializing GlobalMethodSecurityConfiguration with BauhausMethodSecurityExpressionHandler and DefaultRolePrefix = {}",DEFAULT_ROLE_PREFIX);
            var expressionHandler = new BauhausMethodSecurityExpressionHandler(this.stampAuthorizationChecker, this.stampFromPrincipal);
            expressionHandler.setDefaultRolePrefix(DEFAULT_ROLE_PREFIX);
            return expressionHandler;
        }

    }

}
