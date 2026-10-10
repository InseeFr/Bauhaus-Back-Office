package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Vue allégée d'une liste de codes mutualisée, pour l'affichage en lecture seule : sa valeur et son
 * libellé par code, sans le DDI4 complet (URN répétés, catégories à part…) qui pèse ~15 fois plus.
 */
public record MutualizedCodeListCodes(String agencyId, String id, String version, String label, List<Entry> codes) {

    /** Un code : identifiant, valeur, libellé de sa catégorie. */
    public record Entry(String id, String value, String label) {}

    /**
     * Projette la première CodeList de {@code response} : codes de premier niveau dans leur ordre, et
     * libellés pris dans {@code language} (à défaut, le premier disponible ; vide sans catégorie).
     * {@code null} quand la réponse ne porte aucune CodeList.
     */
    public static MutualizedCodeListCodes from(Ddi4Response response, String language) {
        if (response.codeList() == null || response.codeList().isEmpty()) {
            return null;
        }
        Ddi4CodeList codeList = response.codeList().getFirst();

        Map<String, String> categoryLabelById = new HashMap<>();
        if (response.category() != null) {
            for (Ddi4Category category : response.category()) {
                categoryLabelById.put(category.id(), labelIn(category.label(), language));
            }
        }

        List<Entry> codes = codeList.code() == null
                ? List.of()
                : codeList.code().stream()
                        .map(code -> new Entry(
                                code.id(),
                                code.value() == null ? "" : code.value().stringValue(),
                                code.categoryReference() == null
                                        ? ""
                                        : categoryLabelById.getOrDefault(
                                                code.categoryReference().id(), "")))
                        .toList();

        return new MutualizedCodeListCodes(
                codeList.agency(), codeList.id(), codeList.version(), labelIn(codeList.label(), language), codes);
    }

    private static String labelIn(List<LangString> labels, String language) {
        if (labels == null || labels.isEmpty()) {
            return "";
        }
        return labels.stream()
                .filter(label -> Objects.equals(label.language(), language))
                .findFirst()
                .orElse(labels.getFirst())
                .value();
    }
}
