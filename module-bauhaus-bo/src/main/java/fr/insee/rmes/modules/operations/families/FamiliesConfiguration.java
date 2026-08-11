package fr.insee.rmes.modules.operations.families;

import fr.insee.rmes.modules.operations.families.domain.port.clientside.FamilyService;
import fr.insee.rmes.modules.operations.families.domain.port.serverside.OperationFamilyRepository;
import fr.insee.rmes.modules.operations.families.domain.DomainFamilyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FamiliesConfiguration {
    @Bean
    FamilyService familyService(OperationFamilyRepository operationFamilyRepository){
        return new DomainFamilyService(operationFamilyRepository);
    }
}
