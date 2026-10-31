package fr.insee.rmes.modules.commons.configuration;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.commons.healthcheck.DependencyProbe;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Sondes du healthcheck des services externes dont le client vit hors de ce module (MinIO : {@link StorageConfiguration}). */
@Configuration
public class HealthcheckConfiguration {

    /** Désactivable pour les environnements sans Colectica (e2e) : sinon le healthcheck y reste en 500. */
    @Bean
    @ConditionalOnProperty(
            name = "fr.insee.rmes.bauhaus.colectica.healthcheck-enabled",
            havingValue = "true",
            matchIfMissing = true)
    public DependencyProbe colecticaProbe(ColecticaClient colecticaClient) {
        return new DependencyProbe("Colectica", colecticaClient::ping);
    }
}
