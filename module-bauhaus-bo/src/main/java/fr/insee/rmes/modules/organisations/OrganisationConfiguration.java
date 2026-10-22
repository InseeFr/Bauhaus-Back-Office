package fr.insee.rmes.modules.organisations;

import fr.insee.rmes.modules.organisations.domain.DomainOrganisationService;
import fr.insee.rmes.modules.organisations.domain.DomainOrganisationsService;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationService;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationRepository;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import fr.insee.rmes.modules.organisations.infrastructure.CachingOrganisationIdentifiersRepository;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OrganisationsProperties.class)
public class OrganisationConfiguration {

    private static final Duration IDENTIFIERS_TTL = Duration.ofHours(1);

    @Bean
    OrganisationService organisationService(OrganisationRepository organisationRepository) {
        return new DomainOrganisationService(organisationRepository);
    }

    @Bean
    OrganisationsService organisationsService(OrganisationsRepository organisationRepository) {
        // Les timbres de l'utilisateur sont complétés à chaque requête : la correspondance est mémoïsée.
        return new DomainOrganisationsService(
                new CachingOrganisationIdentifiersRepository(organisationRepository, IDENTIFIERS_TTL));
    }
}
