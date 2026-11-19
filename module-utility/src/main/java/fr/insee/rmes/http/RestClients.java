package fr.insee.rmes.http;

import java.time.Duration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.web.client.RestClient;

/**
 * Builds the {@link RestClient}s that reach external services (Keycloak, Colectica). Without explicit
 * timeouts, a service that accepts the connection but never answers blocks the calling request thread
 * forever.
 */
public final class RestClients {

    private RestClients() {}

    public static RestClient withTimeouts(Duration connectTimeout, Duration readTimeout) {
        var requestFactory = ClientHttpRequestFactoryBuilder.detect()
                .build(HttpClientSettings.defaults().withTimeouts(connectTimeout, readTimeout));
        return RestClient.builder().requestFactory(requestFactory).build();
    }
}
