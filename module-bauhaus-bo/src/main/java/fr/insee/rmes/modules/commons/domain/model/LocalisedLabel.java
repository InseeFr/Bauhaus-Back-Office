package fr.insee.rmes.modules.commons.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.MalformedLocalisedLabelException;
import org.apache.commons.lang3.StringUtils;

public record LocalisedLabel(String value, Lang lang) {
    public LocalisedLabel {
        if(StringUtils.isAllBlank(value)){
            throw new MalformedLocalisedLabelException();
        }
    }

    public static LocalisedLabel ofDefaultLanguage(String label){
        return new LocalisedLabel(label, Lang.defaultLanguage());
    }

    public static LocalisedLabel ofAlternativeLanguage(String label){
        return new LocalisedLabel(label, Lang.alternativeLanguage());
    }
}
