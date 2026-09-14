package fr.insee.rmes.colectica.client.auth;

import java.util.function.Supplier;

/**
 * How the {@code ColecticaClient} authenticates against the Colectica API.
 *
 * <ul>
 *   <li>{@link UserPassword} — the client obtains a token from {@code /token/createtoken} with these
 *       credentials, caches it, and re-authenticates on a 401/403.</li>
 *   <li>{@link BearerToken} — the client takes the bearer token from {@code tokenSupplier} on each
 *       request. The supplier owns token lifetime/refresh (e.g. a Keycloak service); a static token is
 *       just {@code () -> "the-token"}. On a 401/403 the client runs {@code onInvalidate} before
 *       re-querying the supplier, so a cached/revoked token can be dropped and a fresh one issued.</li>
 * </ul>
 */
public sealed interface ColecticaCredentials
        permits ColecticaCredentials.UserPassword, ColecticaCredentials.BearerToken {

    record UserPassword(String username, String password) implements ColecticaCredentials {}

    record BearerToken(Supplier<String> tokenSupplier, Runnable onInvalidate) implements ColecticaCredentials {

        /** Static / self-refreshing supplier with no invalidation hook. */
        public BearerToken(Supplier<String> tokenSupplier) {
            this(tokenSupplier, () -> {
                /* no-op */
            });
        }
    }
}
