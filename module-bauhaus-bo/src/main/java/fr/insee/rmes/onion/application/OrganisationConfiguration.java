package fr.insee.rmes.onion.application;

import fr.insee.rmes.domain.port.clientside.CheckerService;
import fr.insee.rmes.domain.port.clientside.OrganisationService;
import fr.insee.rmes.domain.port.serverside.RuleChecker;
import fr.insee.rmes.domain.services.CheckerServiceImpl;
import fr.insee.rmes.domain.services.OrganisationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OrganisationConfiguration {
    @Bean
    OrganisationService organisationService(){
        return new OrganisationServiceImpl();
    }
}
