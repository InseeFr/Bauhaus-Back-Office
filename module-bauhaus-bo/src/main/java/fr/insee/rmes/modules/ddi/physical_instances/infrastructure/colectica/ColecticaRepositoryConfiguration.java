package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration class that creates two separate DDIRepository beans,
 * one for the primary Colectica instance and one for the secondary instance.
 */
@Configuration
public class ColecticaRepositoryConfiguration {

    /**
     * Creates the primary DDIRepository bean using the primary Colectica instance configuration.
     * This bean is marked as @Primary to be used as the default when no qualifier is specified.
     */
    @Bean
    @Primary
    @Qualifier("primaryDDIRepository")
    public DDIRepository primaryDDIRepository(
            RestTemplate restTemplate,
            ColecticaConfiguration colecticaConfiguration,
            ObjectMapper objectMapper,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter
    ) {
        return new DDIRepositoryImpl(
                restTemplate,
                colecticaConfiguration.primary(),
                objectMapper,
                ddi3ToDdi4Converter
        );
    }

    /**
     * Creates the secondary DDIRepository bean using the secondary Colectica instance configuration.
     */
    @Bean
    @Qualifier("secondaryDDIRepository")
    public DDIRepository secondaryDDIRepository(
            RestTemplate restTemplate,
            ColecticaConfiguration colecticaConfiguration,
            ObjectMapper objectMapper,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter
    ) {
        return new DDIRepositoryImpl(
                restTemplate,
                colecticaConfiguration.secondary(),
                objectMapper,
                ddi3ToDdi4Converter
        );
    }
}