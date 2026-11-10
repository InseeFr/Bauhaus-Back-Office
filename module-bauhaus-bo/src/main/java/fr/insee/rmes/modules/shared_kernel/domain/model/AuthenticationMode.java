package fr.insee.rmes.modules.shared_kernel.domain.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * How requests are authenticated, derived from {@code fr.insee.rmes.bauhaus.env}. Each mode lists
 * the values it accepts: an unknown value is rejected rather than silently falling back to the
 * development mode, which hands every request an ADMIN user.
 */
public enum AuthenticationMode {
    /** Local development: no token, every request runs as a fake ADMIN user. */
    DEV("NoAuth", "local"),
    /** Deployed platforms: an OIDC token is required. */
    OIDC("pre-prod", "prod");

    private final Set<String> envValues;

    AuthenticationMode(String... envValues) {
        this.envValues = Set.of(envValues);
    }

    public static AuthenticationMode fromEnv(String env) {
        return Arrays.stream(values())
                .filter(mode -> mode.envValues.stream().anyMatch(value -> value.equalsIgnoreCase(env)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("fr.insee.rmes.bauhaus.env has an unknown value '" + env
                        + "', expected one of " + acceptedValues()));
    }

    private static String acceptedValues() {
        return Arrays.stream(values())
                .flatMap(mode -> mode.envValues.stream())
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
