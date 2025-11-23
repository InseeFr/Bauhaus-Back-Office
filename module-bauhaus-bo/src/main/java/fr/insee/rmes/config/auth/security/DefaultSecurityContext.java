package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.config.auth.user.FakeUserConfiguration;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@ConditionalOnExpression("!'PROD'.equalsIgnoreCase('${fr.insee.rmes.bauhaus.env}')")
public class DefaultSecurityContext {

    private static final List<String> DEFAULT_FAKE_ROLES = List.of(Roles.ADMIN);
    private static final String DEFAULT_FAKE_NAME = "fakeUser";
    private static final Stamp DEFAULT_FAKE_STAMP = new Stamp("DG57-C003");

    private static final Logger logger = LoggerFactory.getLogger(DefaultSecurityContext.class);
    private final boolean requiresSsl;
    private final User fakeUser;

    public DefaultSecurityContext(@Value("${fr.insee.rmes.bauhaus.force.ssl}") boolean requiresSsl, FakeUserConfiguration fakeUserConfiguration) {
        this.requiresSsl = requiresSsl;
        this.fakeUser = initFakeUser(fakeUserConfiguration);
    }

    private User initFakeUser(FakeUserConfiguration fakeUserConfiguration) {
        String fakeName = fakeUserConfiguration.name().orElse(DEFAULT_FAKE_NAME);
        List<String> fakeRoles = fakeUserConfiguration.roles();
        if (fakeRoles.isEmpty()) {
            fakeRoles = DEFAULT_FAKE_ROLES;
        }
        Stamp fakeStamp = fakeUserConfiguration.stamp().map(Stamp::new).orElse(DEFAULT_FAKE_STAMP);
        return new User(fakeName, fakeRoles, fakeStamp, Source.INSEE);
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .anonymous(anonymous -> {
                    anonymous.authorities(fakeUser.roles().toArray(String[]::new));
                    anonymous.principal(fakeUser.id());
                })
                .authorizeHttpRequests(
                        authorizeHttpRequest -> authorizeHttpRequest.anyRequest().permitAll());
        if (requiresSsl) {
            http.requiresChannel(channel -> channel.requestMatchers("/**").requiresSecure());
        }

		logger.info("Default authentication activated - no auth ");

		return http.build();

	}

}