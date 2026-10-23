package fr.insee.rmes.modules.concepts.collections.webservice;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocalisedLabelResponse(
        @NotNull(message = REQUIRED) String value,
        @NotBlank(message = REQUIRED) String lang) {
    static LocalisedLabelResponse fromDomain(LocalisedLabel label) {
        return new LocalisedLabelResponse(label.value(), label.lang().toString());
    }

    public LocalisedLabel toDomain() {
        return new LocalisedLabel(value, Lang.valueOf(lang.toUpperCase()));
    }
}
