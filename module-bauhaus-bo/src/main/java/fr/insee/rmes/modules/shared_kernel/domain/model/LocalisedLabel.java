package fr.insee.rmes.modules.shared_kernel.domain.model;

public record LocalisedLabel(String value, Lang lang) {

    public static LocalisedLabel ofDefaultLanguage(String label) {
        return new LocalisedLabel(label, Lang.defaultLanguage());
    }

    public static LocalisedLabel ofAlternativeLanguage(String label) {
        return new LocalisedLabel(label, Lang.alternativeLanguage());
    }
}
