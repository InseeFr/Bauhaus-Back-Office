package fr.insee.rmes.modules.users;

import fr.insee.rmes.modules.users.domain.DomainUserService;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.modules.users.infrastructure.FakeUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {

    @Bean
    @ConditionalOnExpression("!'PROD'.equalsIgnoreCase('${fr.insee.rmes.bauhaus.env}')")
    public UserDecoder getDevUserProvider() {
        return new FakeUserDecoder();
    }

    @Bean
    @ConditionalOnExpression("'PROD'.equalsIgnoreCase('${fr.insee.rmes.bauhaus.env}')")
    public UserDecoder getProdUserProvider(JwtProperties jwtProperties) {
        return new OidcUserDecoder(jwtProperties);
    }

    @Bean
    UserService userService(UserDecoder decoder){
        return new DomainUserService(decoder);
    }
}
