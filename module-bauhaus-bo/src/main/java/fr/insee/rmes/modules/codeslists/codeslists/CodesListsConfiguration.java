package fr.insee.rmes.modules.codeslists.codeslists;

import fr.insee.rmes.modules.codeslists.codeslists.domain.DomainCodesListsService;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside.CodesListsService;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.serverside.CodesListsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodesListsConfiguration {

    @Bean
    CodesListsService codesListsService(CodesListsRepository repository) {
        return new DomainCodesListsService(repository);
    }
}
