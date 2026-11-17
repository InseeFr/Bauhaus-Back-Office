package fr.insee.rmes.graphdb;

import java.time.Duration;
import org.apache.http.client.config.RequestConfig;
import org.eclipse.rdf4j.http.client.SharedHttpClientSessionManager;

/**
 * Client HTTP partagé par tous les dépôts RDF. RDF4J laisse par défaut une requête attendre dix jours une réponse
 * qui ne vient pas : le délai de socket est ramené à {@code socketTimeout}. Le reste de la configuration RDF4J (pool,
 * relances sur connexion périmée et sur 408) est conservé.
 */
class RdfHttpClientSessionManager extends SharedHttpClientSessionManager {

    private final int socketTimeoutMillis;

    RdfHttpClientSessionManager(Duration socketTimeout) {
        this.socketTimeoutMillis = Math.toIntExact(socketTimeout.toMillis());
    }

    @Override
    public RequestConfig getDefaultRequestConfig() {
        return RequestConfig.copy(super.getDefaultRequestConfig())
                .setSocketTimeout(socketTimeoutMillis)
                .build();
    }
}
