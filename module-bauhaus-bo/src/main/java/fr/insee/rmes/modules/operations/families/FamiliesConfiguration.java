package fr.insee.rmes.modules.operations.families;

import fr.insee.rmes.domain.port.clientside.FamilyService;
import fr.insee.rmes.domain.port.serverside.OperationFamilyRepository;
import fr.insee.rmes.domain.services.operations.FamilyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FamiliesConfiguration {
    @Bean
    FamilyService familyService(OperationFamilyRepository operationFamilyRepository){
        return new FamilyServiceImpl(operationFamilyRepository);
    }
}
