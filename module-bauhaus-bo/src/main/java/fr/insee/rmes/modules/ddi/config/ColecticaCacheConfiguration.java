package fr.insee.rmes.modules.ddi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaCacheNames;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Objects;

/**
 * Enables Spring's caching abstraction and provides a Caffeine-backed {@link CacheManager} for the
 * mutualized code lists caches.
 *
 * <p>A single in-process manager covers {@link ColecticaCacheNames#MUTUALIZED_CODES_LISTS} and
 * {@link ColecticaCacheNames#MUTUALIZED_PACKAGE_CODE_LIST_REFS}. Both expire {@code expireAfterWrite}
 * after the TTL configured by {@code fr.insee.rmes.bauhaus.colectica.mutualized-codes-cache-ttl}
 * (default 24h), so the cache behaves exactly like the previous hand-rolled cache but through the
 * standard Spring Cache abstraction.
 */
@Configuration
// proxyTargetClass = true forces CGLIB (concrete-class) proxies for @Cacheable beans, matching Spring
// Boot's global AOP default. Without it, a standalone context (e.g. @SpringJUnitConfig tests) would
// JDK-proxy beans that implement an interface, breaking injection by their concrete type.
@EnableCaching(proxyTargetClass = true)
public class ColecticaCacheConfiguration {

    @Bean
    public CacheManager colecticaCacheManager(ColecticaConfiguration colecticaConfiguration) {
        Duration ttl = Objects.requireNonNullElse(
            colecticaConfiguration.mutualizedCacheTtl(),
            ColecticaConfiguration.DEFAULT_MUTUALIZED_CACHE_TTL
        );
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            ColecticaCacheNames.MUTUALIZED_CODES_LISTS,
            ColecticaCacheNames.MUTUALIZED_PACKAGE_CODE_LIST_REFS,
            ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS
        );
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(ttl));
        return cacheManager;
    }
}
