package fr.insee.rmes.modules.commons.domain.model;

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
