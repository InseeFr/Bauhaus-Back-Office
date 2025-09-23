package fr.insee.rmes.model.concepts;

import fr.insee.rmes.infrastructure.utils.DiacriticSorter;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record ConceptForAdvancedSearch(
        String id,
        String label,
        String created,
        String modified,
        String disseminationStatus,
        String validationStatus,
        String definition,
        String creator,
        String isTopConceptOf,
        String valid,
        String altLabel) implements DiacriticSorter.AppendableLabel<ConceptForAdvancedSearch>, ConceptForAdvancedSearchBuilder.With {
}

