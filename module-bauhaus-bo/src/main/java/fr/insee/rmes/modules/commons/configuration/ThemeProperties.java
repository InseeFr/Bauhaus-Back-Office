package fr.insee.rmes.modules.commons.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Les deux proprietes decident a elles seules si la liste des themes est peuplee : on echoue au demarrage
 * plutot que de renvoyer une erreur au premier appel de /themes. La validation est faite a la main car
 * aucune implementation de Bean Validation n'est presente au classpath (@NotBlank serait ignore).
 */
@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.theme")
public record ThemeProperties(String graph, String type) {

    private static final String PREFIX = "fr.insee.rmes.bauhaus.theme.";

    public ThemeProperties {
        requireConfigured(graph, PREFIX + "graph");
        requireConfigured(type, PREFIX + "type");
    }

    private static void requireConfigured(String value, String property) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(property + " must be configured");
        }
    }
}
