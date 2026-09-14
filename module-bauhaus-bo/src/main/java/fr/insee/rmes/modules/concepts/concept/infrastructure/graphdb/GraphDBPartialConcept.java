package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.PartialConcept;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;

public record GraphDBPartialConcept(
        String id, String label, @Nullable String altLabel) {

    /**
     * The listing query returns one row per alternative label: rows of a same concept are merged
     * so that every alternative label stays searchable from the concepts list.
     */
    static final String ALT_LABEL_SEPARATOR = " || ";

    GraphDBPartialConcept mergeAltLabelOf(GraphDBPartialConcept other) {
        if (isBlank(other.altLabel)) {
            return this;
        }
        if (isBlank(this.altLabel)) {
            return new GraphDBPartialConcept(id, label, other.altLabel);
        }
        return new GraphDBPartialConcept(id, label, this.altLabel + ALT_LABEL_SEPARATOR + other.altLabel);
    }

    PartialConcept toDomain() {
        return new PartialConcept(
                new ConceptId(id),
                LocalisedLabel.ofDefaultLanguage(label),
                isBlank(altLabel) ? null : LocalisedLabel.ofDefaultLanguage(altLabel));
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }
}
