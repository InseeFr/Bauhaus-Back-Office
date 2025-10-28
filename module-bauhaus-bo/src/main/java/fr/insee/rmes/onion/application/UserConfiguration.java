package fr.insee.rmes.onion.application;

import fr.insee.rmes.domain.port.clientside.OrganisationService;
import fr.insee.rmes.domain.port.clientside.UserService;
import fr.insee.rmes.domain.port.serverside.UserDecoder;
import fr.insee.rmes.domain.services.OrganisationServiceImpl;
import fr.insee.rmes.domain.services.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {
    @Bean
    UserService userService(UserDecoder decoder){
        return new UserServiceImpl(decoder);
    }
}
