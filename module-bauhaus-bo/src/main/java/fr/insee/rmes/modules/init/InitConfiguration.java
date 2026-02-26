package fr.insee.rmes.modules.init;

import fr.insee.rmes.BauhausConfiguration;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.init.domain.DomainInitService;
import fr.insee.rmes.modules.init.domain.port.clientside.InitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InitConfiguration {

    @Bean
    InitService getInitService(
            BauhausConfiguration bauhausConfiguration,
            @Value("${fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote}") String maxLengthScopeNote,
            @Value("${fr.insee.rmes.bauhaus.concepts.defaultContributor}") String defaultContributor,
            @Value("${fr.insee.rmes.bauhaus.validation.operation_series}") List<String> extraMandatoryFields,
            ColecticaConfiguration colecticaConfiguration) {
        return new DomainInitService(
                bauhausConfiguration,
                maxLengthScopeNote,
                defaultContributor,
                extraMandatoryFields,
                colecticaConfiguration
        );
    }
}
