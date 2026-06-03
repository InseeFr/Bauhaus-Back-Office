package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

/**
 * Comparateurs de tri des listes DDI par label.
 * <p>
 * Utilise un {@link Collator} français en force {@link Collator#PRIMARY} (insensible à
 * la casse et aux accents) et est null-safe (un label {@code null} est traité comme une
 * chaîne vide). Un nouveau {@code Collator} est créé à chaque appel car cette classe
 * n'est pas thread-safe.
 */
final class LabelComparators {

    private LabelComparators() {
    }

    static <T> Comparator<T> byLabelAscending(Function<T, String> labelExtractor) {
        Collator collator = Collator.getInstance(Locale.FRENCH);
        collator.setStrength(Collator.PRIMARY);
        Function<T, String> nullSafe = item -> {
            String label = labelExtractor.apply(item);
            return label == null ? "" : label;
        };
        return Comparator.comparing(nullSafe, collator);
    }

    static <T> Comparator<T> byLabelDescending(Function<T, String> labelExtractor) {
        return byLabelAscending(labelExtractor).reversed();
    }
}
