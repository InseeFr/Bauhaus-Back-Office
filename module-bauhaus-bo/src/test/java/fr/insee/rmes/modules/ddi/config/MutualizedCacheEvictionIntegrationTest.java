package fr.insee.rmes.modules.ddi.config;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaCacheNames;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.DDIRepositoryImpl;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.MutualizedCodeListRefsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies that {@link DDIRepository#evictMutualizedCodesListsCache()} is actually intercepted through
 * the Spring proxy ({@code @CacheEvict}) and clears <em>both</em> mutualized cache regions backing the
 * mutualized codes list: the high-level list ({@link ColecticaCacheNames#MUTUALIZED_CODES_LISTS}) and
 * the underlying package CodeList references ({@link ColecticaCacheNames#MUTUALIZED_PACKAGE_CODE_LIST_REFS}).
 * This is what the {@code Cache-Control: no-cache} header on {@code GET /ddi/mutualized-codes-list}
 * relies on to force a fresh walk of Colectica.
 */
@SpringJUnitConfig
class MutualizedCacheEvictionIntegrationTest {

    @Configuration
    @Import(ColecticaCacheConfiguration.class)
    static class TestConfig {

        @Bean
        ColecticaConfiguration colecticaConfiguration() {
            var server = new ColecticaConfiguration.ColecticaInstanceConfiguration(
                "https://example.com", "/api/v1/",
                Map.of("CodeListScheme", "s", "CodeListGroup", "g", "CodeList", "c"),
                "resp", "format", "password", "user", "pass", "fr.insee");
            return new ColecticaConfiguration(
                List.of("fr-FR"), server,
                new ColecticaConfiguration.PackageRef("fr.insee", "pkg-1", 1),
                Duration.ofHours(1));
        }

        @Bean
        DDIRepository ddiRepository(ColecticaConfiguration config) {
            var provider = new MutualizedCodeListRefsProvider(
                config.server(), config, mock(ColecticaClient.class));
            return new DDIRepositoryImpl(
                config.server(),
                mock(DDI3toDDI4ConverterService.class),
                mock(DDI4toDDI3ConverterService.class),
                config,
                mock(ColecticaClient.class),
                provider);
        }
    }

    @Autowired
    DDIRepository ddiRepository;

    @Autowired
    CacheManager cacheManager;

    @Test
    void evictMutualizedCodesListsCache_clearsBothCacheRegions() {
        Cache codesLists = cacheManager.getCache(ColecticaCacheNames.MUTUALIZED_CODES_LISTS);
        Cache packageRefs = cacheManager.getCache(ColecticaCacheNames.MUTUALIZED_PACKAGE_CODE_LIST_REFS);
        assertThat(codesLists).isNotNull();
        assertThat(packageRefs).isNotNull();
        codesLists.put("k", List.of());
        packageRefs.put("k", List.of());

        ddiRepository.evictMutualizedCodesListsCache();

        assertThat(codesLists.get("k")).isNull();
        assertThat(packageRefs.get("k")).isNull();
    }

    @Test
    void evictPhysicalInstanceSearchRowsCache_clearsTheSearchRowsRegionOnly() {
        Cache searchRows = cacheManager.getCache(ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS);
        Cache codesLists = cacheManager.getCache(ColecticaCacheNames.MUTUALIZED_CODES_LISTS);
        assertThat(searchRows).isNotNull();
        assertThat(codesLists).isNotNull();
        searchRows.put("k", List.of());
        codesLists.put("k", List.of());

        ddiRepository.evictPhysicalInstanceSearchRowsCache();

        assertThat(searchRows.get("k")).isNull();
        assertThat(codesLists.get("k")).isNotNull();
    }
}
