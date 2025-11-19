package fr.insee.rmes.modules.operations.msd;

import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.DocumentationRepository;
import fr.insee.rmes.modules.operations.msd.domain.DocumentationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class MSDConfiguration {
    @Bean
    DocumentationService documentationService(DocumentationRepository repository){
        return new DocumentationServiceImpl(repository);
    }
}
