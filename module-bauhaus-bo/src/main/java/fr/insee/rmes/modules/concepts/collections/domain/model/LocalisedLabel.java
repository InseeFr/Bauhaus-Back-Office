package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.MalformedLocalisedLabelException;
import org.apache.commons.lang3.StringUtils;

public record LocalisedLabel(String label, Lang lang) {
    public LocalisedLabel {
        if(StringUtils.isAllBlank(label)){
            throw new MalformedLocalisedLabelException();
        }
    }
}
