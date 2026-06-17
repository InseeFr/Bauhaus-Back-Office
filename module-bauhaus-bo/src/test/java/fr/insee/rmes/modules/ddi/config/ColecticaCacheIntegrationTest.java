package fr.insee.rmes.modules.ddi.config;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.MutualizedCodeListRefsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that the {@code @Cacheable} walk of {@link MutualizedCodeListRefsProvider} is actually
 * intercepted through the Spring proxy and backed by the real {@link ColecticaCacheConfiguration}
 * (Caffeine). This is the integration counterpart of the cache that used to be hand-rolled inside
 * {@code DDIRepositoryImpl}: a second call must be served from the cache, so Colectica is hit once.
 */
@SpringJUnitConfig
class ColecticaCacheIntegrationTest {

    private static final String SCHEME_TYPE = "scheme-type";
    private static final String GROUP_TYPE = "group-type";
    private static final String CODE_LIST_TYPE = "code-list-type";

    @Configuration
    @Import(ColecticaCacheConfiguration.class)
    static class TestConfig {

        @Bean
        ColecticaClient colecticaClient() {
            return mock(ColecticaClient.class);
        }

        @Bean
        ColecticaConfiguration colecticaConfiguration() {
            var server = new ColecticaConfiguration.ColecticaInstanceConfiguration(
                "https://example.com", "/api/v1/",
                Map.of("CodeListScheme", SCHEME_TYPE, "CodeListGroup", GROUP_TYPE, "CodeList", CODE_LIST_TYPE),
                "resp", "format", "password", "user", "pass", "fr.insee");
            return new ColecticaConfiguration(
                List.of("fr-FR"), server,
                new ColecticaConfiguration.PackageRef("fr.insee", "pkg-1", 1),
                Duration.ofHours(1));
        }

        @Bean
        MutualizedCodeListRefsProvider mutualizedCodeListRefsProvider(
                ColecticaConfiguration config, ColecticaClient client) {
            return new MutualizedCodeListRefsProvider(config.server(), config, client);
        }
    }

    @Autowired
    MutualizedCodeListRefsProvider provider;

    @Autowired
    ColecticaClient client;

    @Test
    void secondCall_isServedFromCache() {
        var pkg = new ItemReference("fr.insee", "pkg-1");
        var scheme = new ItemReference("fr.insee", "scheme-1");
        var group = new ItemReference("fr.insee", "group-1");
        var codeList = new ItemReference("fr.insee", "cl-1");

        when(client.findRelatedDescriptions(RelationshipDirection.BY_SUBJECT, pkg, List.of(SCHEME_TYPE)))
            .thenReturn(List.of(scheme));
        when(client.findRelatedDescriptions(RelationshipDirection.BY_SUBJECT, scheme, List.of(GROUP_TYPE)))
            .thenReturn(List.of(group));
        when(client.findRelatedDescriptions(RelationshipDirection.BY_SUBJECT, group, List.of(CODE_LIST_TYPE)))
            .thenReturn(List.of(codeList));

        List<ItemReference> first = provider.codeListRefs();
        List<ItemReference> second = provider.codeListRefs();

        assertThat(first).containsExactly(codeList);
        assertThat(second).isEqualTo(first);

        // The package tree is walked exactly once across both invocations (second served from cache).
        verify(client, times(1)).findRelatedDescriptions(
            eq(RelationshipDirection.BY_SUBJECT), eq(pkg), anyList());
    }
}
