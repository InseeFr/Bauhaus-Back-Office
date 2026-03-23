package fr.insee.rmes.modules.commons;

import fr.insee.rmes.modules.commons.configuration.ThemeProperties;
import fr.insee.rmes.modules.commons.domain.port.clientside.DisseminationStatusService;
import fr.insee.rmes.modules.commons.domain.port.clientside.ThemeService;
import fr.insee.rmes.modules.commons.domain.port.serverside.ThemeRepository;
import fr.insee.rmes.modules.commons.domain.service.DomainDisseminationStatusService;
import fr.insee.rmes.modules.commons.domain.service.DomainThemeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonsConfiguration {
    @Bean
    DisseminationStatusService getDisseminationService() {
        return new DomainDisseminationStatusService();
    }

    @Bean
    ThemeService getThemeService(
            ThemeRepository themeRepository,
            ThemeProperties themeProperties
     ) {
        return new DomainThemeService(themeRepository, themeProperties.conceptSchemeFilter());
    }

}
