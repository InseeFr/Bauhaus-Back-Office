package fr.insee.rmes.modules.commons;

import fr.insee.rmes.modules.commons.domain.port.clientside.DisseminationStatusService;
import fr.insee.rmes.modules.commons.domain.service.DomainDisseminationStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonsConfiguration {
    @Bean
    DisseminationStatusService getDisseminationService() {
        return new DomainDisseminationStatusService();
    }
}
