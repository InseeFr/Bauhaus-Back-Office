package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

public final class MultilingualStrings {

    private MultilingualStrings() {
    }

    public static List<MultilingualStringEntry> of(String languageTag, String value) {
        return List.of(new MultilingualStringEntry(new MultilingualStringValue(languageTag, value)));
    }
}
