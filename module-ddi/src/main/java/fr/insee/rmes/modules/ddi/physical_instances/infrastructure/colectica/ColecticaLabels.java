package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.LocalizedText;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Extraction des libellés multilingues portés par les items Colectica, dans la langue par défaut de
 * l'instance (première de {@code ColecticaConfiguration.langs()}), puis l'anglais, puis n'importe
 * quelle autre.
 */
final class ColecticaLabels {

    private final String defaultLang;

    ColecticaLabels(String defaultLang) {
        this.defaultLang = defaultLang;
    }

    String defaultLang() {
        return defaultLang;
    }

    /** Libellé d'affichage d'un item : {@code itemName}, sinon {@code label}, sinon l'identifiant. */
    String of(ColecticaItem item) {
        String label = fromLanguageMap(item.itemName());
        if (label == null || label.trim().isEmpty()) {
            label = fromLanguageMap(item.label());
        }
        if (label == null || label.trim().isEmpty()) {
            label = item.identifier();
        }
        return label;
    }

    /**
     * Extraction stricte pour les listes de codes mutualisées : première valeur non vide de
     * {@code label} puis {@code itemName}. Le libellé prime sur le nom technique car c'est lui qui
     * s'affiche dans le sélecteur. Aucun repli sur l'identifiant : {@link Optional#empty()} quand
     * aucune valeur non vide n'existe.
     */
    Optional<String> strict(ColecticaItem item) {
        return firstNonBlank(item.label()).or(() -> firstNonBlank(item.itemName()));
    }

    Optional<String> firstNonBlank(Map<String, String> languageMap) {
        if (languageMap == null) {
            return Optional.empty();
        }
        String preferred = languageMap.get(defaultLang);
        if (preferred != null && !preferred.isBlank()) {
            return Optional.of(preferred);
        }
        String english = languageMap.get("en");
        if (english != null && !english.isBlank()) {
            return Optional.of(english);
        }
        return languageMap.values().stream()
            .filter(v -> v != null && !v.isBlank())
            .findFirst();
    }

    String fromLanguageMap(Map<String, String> languageMap) {
        if (languageMap == null) {
            return null;
        }
        String label = languageMap.get(defaultLang);
        if (label == null || label.trim().isEmpty()) {
            label = languageMap.get("en");
        }
        if (label == null || label.trim().isEmpty()) {
            label = languageMap.values().stream().findFirst().orElse(null);
        }
        return label;
    }

    /**
     * Libellé d'un item de requête avancée : propriété {@code label}, sinon {@code dcTitle}, en
     * préférant la langue par défaut puis n'importe quelle valeur non vide, avec repli sur
     * l'identifiant — miroir de {@link #of(ColecticaItem)} pour la forme historique.
     */
    String ofAdvanced(ColecticaAdvancedItem item) {
        return firstNonBlankLocalized(item, "label")
            .or(() -> firstNonBlankLocalized(item, "dcTitle"))
            .orElseGet(item::identifier);
    }

    private Optional<String> firstNonBlankLocalized(ColecticaAdvancedItem item, String propertyKey) {
        if (item.textProperties() == null) {
            return Optional.empty();
        }
        List<LocalizedText> values = item.textProperties().get(propertyKey);
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> preferred = values.stream()
            .filter(v -> defaultLang.equals(v.languageTag()))
            .map(LocalizedText::value)
            .filter(v -> v != null && !v.isBlank())
            .findFirst();
        return preferred.or(() -> values.stream()
            .map(LocalizedText::value)
            .filter(v -> v != null && !v.isBlank())
            .findFirst());
    }

    /**
     * Construit un libellé portant {@code newText}, dans la langue du libellé existant s'il y en a un,
     * sinon dans la langue par défaut. Renvoie le libellé existant tel quel quand {@code newText} est
     * {@code null}.
     */
    List<LangString> withFallback(List<LangString> existingLabel, String newText) {
        if (newText == null) {
            return existingLabel;
        }
        String lang = existingLabel != null && !existingLabel.isEmpty()
            ? existingLabel.get(0).language()
            : defaultLang;
        return LangStrings.of(lang, newText);
    }

    static String firstValue(List<LangString> label) {
        return label != null && !label.isEmpty() ? label.get(0).value() : null;
    }
}
