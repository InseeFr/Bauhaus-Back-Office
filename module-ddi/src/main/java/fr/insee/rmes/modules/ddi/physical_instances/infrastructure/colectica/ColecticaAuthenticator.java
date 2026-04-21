package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import java.util.function.Function;

/**
 * Interface for Colectica API authentication.
 * Implementations handle different authentication modes (password or token).
 */
public interface ColecticaAuthenticator {

    /**
     * Execute an API call with authentication.
     * Handles token retrieval and retry on authentication failure.
     *
     * @param apiCall Function that takes a token and performs the API call
     * @param <T> Return type of the API call
     * @return Result of the API call
     */
    <T> T executeWithAuth(Function<String, T> apiCall);
}
