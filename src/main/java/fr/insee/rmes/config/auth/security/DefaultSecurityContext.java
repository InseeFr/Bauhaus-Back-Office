package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.config.auth.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@ConditionalOnExpression("!'PROD'.equalsIgnoreCase('${fr.insee.rmes.bauhaus.env}')")
public class DefaultSecurityContext {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSecurityContext.class);
    private final boolean requiresSsl;

    public DefaultSecurityContext(@Value("${fr.insee.rmes.bauhaus.force.ssl}") boolean requiresSsl) {
        this.requiresSsl = requiresSsl;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .authorizeHttpRequests(
                        authorizeHttpRequest -> authorizeHttpRequest.anyRequest().permitAll());
        if (requiresSsl) {
            http.requiresChannel(channel -> channel.requestMatchers("/**").requiresSecure());
        }

        logger.info("Default authentication activated - no auth ");

        return http.build();

    }

    @Bean
    public UserDecoder getUserProvider() {
        return principal -> Optional.of(User.FAKE_USER);
    }

}