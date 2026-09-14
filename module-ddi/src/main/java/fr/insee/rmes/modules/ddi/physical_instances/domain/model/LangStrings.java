package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

public final class LangStrings {

    private LangStrings() {}

    public static List<LangString> of(String language, String value) {
        return List.of(new LangString(language, value));
    }
}
