package fr.insee.rmes;

import fr.insee.rmes.modules.shared_kernel.domain.model.ConfiguredLanguages;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Les deux langues de l'instance. Toutes les requetes SPARQL et tous les libelles stockes sont tagues
 * avec l'une des deux : on echoue au demarrage plutot que de produire des triplets sans langue. La
 * validation est faite a la main car aucune implementation de Bean Validation n'est presente au
 * classpath (@NotBlank serait ignore).
 */
@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus")
public record BauhausLanguagesProperties(String lg1, String lg2) {

    private static final String PREFIX = "fr.insee.rmes.bauhaus.";

    public BauhausLanguagesProperties {
        requireConfigured(lg1, PREFIX + "lg1");
        requireConfigured(lg2, PREFIX + "lg2");
    }

    public ConfiguredLanguages toDomain() {
        return new ConfiguredLanguages(lg1, lg2);
    }

    private static void requireConfigured(String value, String property) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(property + " must be configured");
        }
    }
}
