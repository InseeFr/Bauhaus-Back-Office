package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;

public record LocalisedLabelResponse(String value, String lang) {
    static LocalisedLabelResponse fromDomain(LocalisedLabel label){
        return new LocalisedLabelResponse(label.value(), label.lang().toString());
    }


    public LocalisedLabel toDomain() {
        return new LocalisedLabel(value, Lang.valueOf(lang.toUpperCase()));
    }
}
