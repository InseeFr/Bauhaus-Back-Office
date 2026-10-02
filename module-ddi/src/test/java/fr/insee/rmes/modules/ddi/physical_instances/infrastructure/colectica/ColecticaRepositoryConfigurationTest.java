package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class ColecticaRepositoryConfigurationTest {

    private static final ColecticaConfiguration COLECTICA_CONFIGURATION = new ColecticaConfiguration(
            List.of("fr-FR"),
            new ColecticaConfiguration.ColecticaInstanceConfiguration(
                    "https://example.com",
                    "/api/v1/",
                    Collections.emptyMap(),
                    "resp",
                    "format",
                    "password",
                    "user",
                    "pass",
                    "fr.insee"),
            null,
            null);

    @Test
    void primaryDDIRepository_usesThePhysicalInstanceSearchRowsCacheRegion() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        DDIRepository repository = new ColecticaRepositoryConfiguration()
                .primaryDDIRepository(
                        COLECTICA_CONFIGURATION,
                        mock(DDI3toDDI4ConverterService.class),
                        mock(DDI4toDDI3ConverterService.class),
                        mock(ColecticaClient.class),
                        mock(MutualizedCodeListRefsStrategy.class),
                        cacheManager);

        assertThat(repository).isInstanceOf(DDIRepositoryImpl.class);
        assertThat(cacheManager.getCacheNames()).containsExactly(ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS);
    }
}
