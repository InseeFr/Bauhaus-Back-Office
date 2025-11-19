package fr.insee.rmes.onion.application;

import fr.insee.rmes.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.domain.port.clientside.DDIService;
import fr.insee.rmes.domain.port.clientside.FamilyService;
import fr.insee.rmes.domain.port.serverside.DDIRepository;
import fr.insee.rmes.domain.port.serverside.OperationFamilyRepository;
import fr.insee.rmes.domain.services.ddi.DDI3toDDI4ConverterServiceImpl;
import fr.insee.rmes.domain.services.ddi.DDI4toDDI3ConverterServiceImpl;
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

    @Bean
    DDI4toDDI3ConverterService ddi4toDdi3ConverterService(){
        return new DDI4toDDI3ConverterServiceImpl();
    }

    @Bean
    DDI3toDDI4ConverterService ddi3toDdi4ConverterService(){
        return new DDI3toDDI4ConverterServiceImpl();
    }
}
