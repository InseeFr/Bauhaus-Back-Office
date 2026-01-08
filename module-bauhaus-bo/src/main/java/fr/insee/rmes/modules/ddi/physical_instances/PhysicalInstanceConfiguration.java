package fr.insee.rmes.modules.ddi.physical_instances;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.DDI3toDDI4ConverterServiceImpl;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.DDI4toDDI3ConverterServiceImpl;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.DDIServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PhysicalInstanceConfiguration {
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
