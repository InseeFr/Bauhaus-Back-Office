package fr.insee.rmes.modules.init;

import fr.insee.rmes.modules.init.domain.DomainInitService;
import fr.insee.rmes.modules.init.domain.port.clientside.InitService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InitConfiguration {

    @Bean
    InitService getInitService(
            @Value("${fr.insee.rmes.bauhaus.env}") String env,
            @Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
            @Value("${fr.insee.rmes.bauhaus.lg2}") String lg2,
            @Value("${fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote}") String maxLengthScopeNote,
            @Value("${fr.insee.rmes.bauhaus.concepts.defaultContributor}") String defaultContributor,
            @Value("${fr.insee.rmes.bauhaus.appHost}") String appHost,
            @Value("${fr.insee.rmes.bauhaus.activeModules}") List<String> activeModules,
            @Value("${fr.insee.rmes.bauhaus.modules}") List<String> modules,
            @Value("${fr.insee.rmes.bauhaus.version}") String version,
            @Value("${fr.insee.rmes.bauhaus.validation.operation_series}") List<String> extraMandatoryFields,
            ColecticaConfiguration colecticaConfiguration) {
        return new DomainInitService(
                env,
                lg1,
                lg2,
                maxLengthScopeNote,
                defaultContributor,
                appHost,
                activeModules,
                modules,
                version,
                extraMandatoryFields,
                colecticaConfiguration
        );
    }
}
