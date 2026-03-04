package fr.insee.rmes.modules.organisations;

import fr.insee.rmes.domain.port.clientside.OrganisationService;
import fr.insee.rmes.domain.port.serverside.OrganisationRepository;
import fr.insee.rmes.domain.services.OrganisationServiceImpl;
import fr.insee.rmes.modules.organisations.domain.DomainOrganisationsService;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OrganisationsProperties.class)
public class OrganisationConfiguration {
    @Bean
    OrganisationService organisationService(OrganisationRepository organisationRepository){
        return new OrganisationServiceImpl(organisationRepository);
    }

    @Bean
    OrganisationsService organisationsService(OrganisationsRepository organisationRepository){
        return new DomainOrganisationsService(organisationRepository);
    }
}
