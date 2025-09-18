package fr.insee.rmes.onion.application;

import fr.insee.rmes.domain.port.clientside.DDIService;
import fr.insee.rmes.domain.port.clientside.FamilyService;
import fr.insee.rmes.domain.port.serverside.DDIRepository;
import fr.insee.rmes.domain.port.serverside.OperationFamilyRepository;
import fr.insee.rmes.domain.services.ddi.DDIServiceImpl;
import fr.insee.rmes.domain.services.operations.FamilyServiceImpl;
import fr.insee.rmes.onion.domain.port.clientside.DocumentationService;
import fr.insee.rmes.onion.domain.port.serverside.DocumentationRepository;
import fr.insee.rmes.onion.domain.services.operations.DocumentationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperationsConfiguration {
    @Bean
    DocumentationService documentationService(DocumentationRepository repository){
        return new DocumentationServiceImpl(repository);
    }

    @Bean
    FamilyService familyService(OperationFamilyRepository operationFamilyRepository){
        return new FamilyServiceImpl(operationFamilyRepository);
    }

    @Bean
    DDIService ddiService(DDIRepository repository){
        return new DDIServiceImpl(repository);
    }
}
