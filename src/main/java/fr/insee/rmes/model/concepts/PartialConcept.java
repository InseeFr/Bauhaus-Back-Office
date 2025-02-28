package fr.insee.rmes.model.concepts;

import fr.insee.rmes.utils.DiacriticSorter;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialConcept(String id, String label,
                             String altLabel) implements DiacriticSorter.AppendableLabel<PartialConcept>, PartialConceptBuilder.With {
}