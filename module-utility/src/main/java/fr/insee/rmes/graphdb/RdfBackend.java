package fr.insee.rmes.graphdb;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Base RDF derrière les dépôts de gestion et de publication, lue depuis {@value #PROPERTY}. Sans valeur, on reste
 * sur GraphDB.
 */
public enum RdfBackend {
    GRAPHDB,
    FUSEKI;

    public static final String PROPERTY = "fr.insee.rmes.rdf.backend";

    public static RdfBackend fromProperty(String value) {
        if (value == null || value.isBlank()) {
            return GRAPHDB;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "%s=%s : valeur inconnue, attendu %s".formatted(PROPERTY, value, acceptedValues()), e);
        }
    }

    private static String acceptedValues() {
        return Arrays.stream(values())
                .map(backend -> backend.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" ou "));
    }
}
