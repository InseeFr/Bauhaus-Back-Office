package fr.insee.rmes.modules.operations.msd;

import fr.insee.rmes.modules.operations.msd.domain.DocumentationExportServiceImpl;
import fr.insee.rmes.modules.operations.msd.domain.DocumentationServiceImpl;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationExportService;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.DocumentationExportGateway;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.DocumentationRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DocumentationConfiguration.class)
public class MSDConfiguration {
    @Bean
    DocumentationService documentationService(DocumentationRepository repository){
        return new DocumentationServiceImpl(repository);
    }

    @Bean
    DocumentationExportService documentationExportService(DocumentationExportGateway gateway) {
        return new DocumentationExportServiceImpl(gateway);
    }
}
