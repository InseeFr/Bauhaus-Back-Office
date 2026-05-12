package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptToValidate;
import org.jspecify.annotations.Nullable;

public record GraphDBConceptToValidate(String id, String label, String creator, @Nullable String valid) {

    ConceptToValidate toDomain() {
        return new ConceptToValidate(new ConceptId(id), label, creator);
    }
}
