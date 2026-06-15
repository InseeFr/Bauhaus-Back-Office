package fr.insee.rmes.keycloak;

public interface TokenService {

    String getAccessToken();

    boolean isTokenValid(String token);

    /**
     * Drops any cached access token so the next {@link #getAccessToken()} fetches a fresh one.
     * Used as a safety net when a downstream call is rejected (401/403) despite a not-yet-expired
     * cached token (e.g. server-side revocation). Default no-op for implementations without a cache.
     */
    default void invalidate() {
        // no-op by default
    }
}
