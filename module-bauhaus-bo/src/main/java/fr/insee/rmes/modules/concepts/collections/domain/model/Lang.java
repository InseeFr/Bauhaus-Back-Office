package fr.insee.rmes.modules.concepts.collections.domain.model;

public enum Lang {
    FR,
    EN;

    public static Lang defaultLanguage() {
        return Lang.FR;
    }

    public static Lang alternativeLanguage() {
        return Lang.EN;
    }
}
