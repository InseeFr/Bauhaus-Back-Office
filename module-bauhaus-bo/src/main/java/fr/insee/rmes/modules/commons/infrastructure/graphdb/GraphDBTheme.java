package fr.insee.rmes.modules.commons.infrastructure.graphdb;

import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.commons.domain.model.Theme;

public record GraphDBTheme(String uri, String label) {
    Theme toDomain() {
        return new Theme(uri, LocalisedLabel.ofDefaultLanguage(label));
    }
}
