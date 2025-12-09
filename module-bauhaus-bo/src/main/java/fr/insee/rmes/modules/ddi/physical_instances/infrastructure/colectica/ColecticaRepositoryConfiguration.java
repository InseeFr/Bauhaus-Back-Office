package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;


@Configuration
public class ColecticaRepositoryConfiguration {

    /**
     * Creates the primary DDIRepository bean using the primary Colectica instance configuration.
     */
    @Bean
    public DDIRepository primaryDDIRepository(
            RestTemplate restTemplate,
            ColecticaConfiguration colecticaConfiguration,
            ObjectMapper objectMapper,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            DDI4toDDI3ConverterService ddi4ToDdi3Converter
    ) {
        return new DDIRepositoryImpl(
                restTemplate,
                colecticaConfiguration.server(),
                objectMapper,
                ddi3ToDdi4Converter,
                ddi4ToDdi3Converter,
                colecticaConfiguration
        );
    }
}