package fr.insee.rmes.modules.shared_kernel.domain.model;

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
