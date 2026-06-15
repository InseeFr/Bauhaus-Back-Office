package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.GroupRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.StudyUnitRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi4ToLifecycle33;
import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.auth.ColecticaCredentials;
import fr.insee.rmes.keycloak.TokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class ColecticaRepositoryConfiguration {

    /**
     * The Colectica SDK owns the only {@link RestClient} used to reach Colectica; the application code
     * talks to Colectica exclusively through this bean.
     */
    @Bean
    public ColecticaClient colecticaClient(
            ColecticaConfiguration colecticaConfiguration,
            @Qualifier("colectica") TokenService colecticaTokenService) {
        var server = colecticaConfiguration.server();
        ColecticaCredentials credentials =
                "token".equals(server.authenticationMode())
                        ? new ColecticaCredentials.BearerToken(
                                colecticaTokenService::getAccessToken, colecticaTokenService::invalidate)
                        : new ColecticaCredentials.UserPassword(server.username(), server.password());
        return new ColecticaClient(RestClient.create(), server.baseApiUrl(), server.baseServerUrl(), credentials);
    }

    @Bean
    public DDIRepository primaryDDIRepository(
            ColecticaConfiguration colecticaConfiguration,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            DDI4toDDI3ConverterService ddi4ToDdi3Converter,
            ColecticaClient colecticaClient
    ) {
        return new DDIRepositoryImpl(
                colecticaConfiguration.server(),
                ddi3ToDdi4Converter,
                ddi4ToDdi3Converter,
                colecticaConfiguration,
                colecticaClient
        );
    }

    @Bean
    public GroupRepository groupRepository(
            ColecticaConfiguration colecticaConfiguration,
            Ddi4ToLifecycle33 ddi4ToLifecycle33,
            DDIRepository ddiRepository,
            ColecticaClient colecticaClient
    ) {
        return new ColecticaGroupRepository(
                colecticaClient,
                colecticaConfiguration.server(),
                ddi4ToLifecycle33,
                ddiRepository
        );
    }

    @Bean
    public StudyUnitRepository studyUnitRepository(
            ColecticaConfiguration colecticaConfiguration,
            Ddi4ToLifecycle33 ddi4ToLifecycle33,
            DDIRepository ddiRepository,
            ColecticaClient colecticaClient
    ) {
        return new ColecticaStudyUnitRepository(
                colecticaClient,
                colecticaConfiguration.server(),
                ddi4ToLifecycle33,
                ddiRepository
        );
    }
}
