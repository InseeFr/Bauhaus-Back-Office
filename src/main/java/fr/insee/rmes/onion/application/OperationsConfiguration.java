package fr.insee.rmes.onion.application;

import fr.insee.rmes.onion.domain.port.clientside.DocumentationService;
import fr.insee.rmes.onion.domain.port.clientside.operations.FamilyService;
import fr.insee.rmes.onion.domain.port.serverside.DocumentationRepository;
import fr.insee.rmes.onion.domain.port.serverside.operations.OperationFamilyRepository;
import fr.insee.rmes.onion.domain.services.operations.DocumentationServiceImpl;
import fr.insee.rmes.onion.domain.services.operations.FamilyServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperationsConfiguration {
    @Bean
    DocumentationService documentationService(DocumentationRepository repository){
        return new DocumentationServiceImpl(repository);
    }

    @Bean
    FamilyService familyService(@Qualifier("graphql") OperationFamilyRepository operationFamilyRepository){
        return new FamilyServiceImpl(operationFamilyRepository);
    }
}
