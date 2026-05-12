package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.concept.domain.model.CompactConcept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;

public record GraphDBPartialConcept(String id, String label, @Nullable String altLabel) {

    CompactConcept toDomain() {
        return new CompactConcept(
                new ConceptId(id),
                new LocalisedLabel(label, Lang.defaultLanguage())
        );
    }
}
