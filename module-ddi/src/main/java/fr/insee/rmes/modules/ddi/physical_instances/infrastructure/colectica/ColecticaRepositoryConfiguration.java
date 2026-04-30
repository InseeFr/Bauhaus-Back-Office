package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.GroupRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.StudyUnitRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi3XmlWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class ColecticaRepositoryConfiguration {

    /**
     * Creates the primary DDIRepository bean using the primary Colectica instance configuration.
     */
    @Bean
    public DDIRepository primaryDDIRepository(
            RestClient restClient,
            ColecticaConfiguration colecticaConfiguration,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            DDI4toDDI3ConverterService ddi4ToDdi3Converter,
            ColecticaAuthenticator authenticator
    ) {
        return new DDIRepositoryImpl(
                restClient,
                colecticaConfiguration.server(),
                ddi3ToDdi4Converter,
                ddi4ToDdi3Converter,
                colecticaConfiguration,
                authenticator
        );
    }

    @Bean
    public GroupRepository groupRepository(
            RestClient restClient,
            ColecticaConfiguration colecticaConfiguration,
            ColecticaAuthenticator authenticator,
            Ddi3XmlWriter ddi3XmlWriter,
            DDIRepository ddiRepository
    ) {
        return new ColecticaGroupRepository(
                restClient,
                colecticaConfiguration.server(),
                authenticator,
                ddi3XmlWriter,
                ddiRepository
        );
    }

    @Bean
    public StudyUnitRepository studyUnitRepository(
            RestClient restClient,
            ColecticaConfiguration colecticaConfiguration,
            ColecticaAuthenticator authenticator,
            Ddi3XmlWriter ddi3XmlWriter,
            DDIRepository ddiRepository
    ) {
        return new ColecticaStudyUnitRepository(
                restClient,
                colecticaConfiguration.server(),
                authenticator,
                ddi3XmlWriter,
                ddiRepository
        );
    }
}
