package fr.insee.rmes;

import fr.insee.rmes.modules.shared_kernel.domain.model.ConfiguredLanguages;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus")
public record BauhausLanguagesProperties(String lg1, String lg2) {

    public ConfiguredLanguages toDomain() {
        return new ConfiguredLanguages(lg1, lg2);
    }
}
