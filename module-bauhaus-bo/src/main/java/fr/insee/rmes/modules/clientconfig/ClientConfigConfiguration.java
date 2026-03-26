package fr.insee.rmes.modules.clientconfig;

import fr.insee.rmes.BauhausConfiguration;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.clientconfig.domain.DomainClientConfigService;
import fr.insee.rmes.modules.clientconfig.domain.port.clientside.ClientConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ClientConfigConfiguration {

    @Bean
    ClientConfigService getClientConfigService(
            BauhausConfiguration bauhausConfiguration,
            @Value("${fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote}") String maxLengthScopeNote,
            @Value("${fr.insee.rmes.bauhaus.concepts.defaultContributor}") String defaultContributor,
            @Value("${fr.insee.rmes.bauhaus.validation.operation_series}") List<String> extraMandatoryFields,
            ColecticaConfiguration colecticaConfiguration) {
        return new DomainClientConfigService(
                bauhausConfiguration.appHost(),
                maxLengthScopeNote,
                defaultContributor,
                bauhausConfiguration.lg1(),
                bauhausConfiguration.lg2(),
                bauhausConfiguration.env(),
                bauhausConfiguration.modules(),
                bauhausConfiguration.version(),
                extraMandatoryFields,
                colecticaConfiguration.server().defaultAgencyId(),
                colecticaConfiguration.langs(),
                bauhausConfiguration.enableDevTools()
        );
    }
}
