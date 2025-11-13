package fr.insee.rmes.modules.organisations;

import fr.insee.rmes.domain.port.clientside.OrganisationService;
import fr.insee.rmes.domain.port.serverside.OrganisationRepository;
import fr.insee.rmes.domain.services.OrganisationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrganisationConfiguration {
    @Bean
    OrganisationService organisationService(OrganisationRepository organisationRepository){
        return new OrganisationServiceImpl(organisationRepository);
    }
}
