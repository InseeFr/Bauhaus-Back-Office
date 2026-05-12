package fr.insee.rmes.modules.concepts.concept.webservice.response;

import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

public record LocalisedLabelResponse(String value, String lang) {

    public static LocalisedLabelResponse fromDomain(LocalisedLabel label) {
        return new LocalisedLabelResponse(label.value(), label.lang().toString());
    }

    public LocalisedLabel toDomain() {
        return new LocalisedLabel(value, Lang.valueOf(lang.toUpperCase()));
    }
}
