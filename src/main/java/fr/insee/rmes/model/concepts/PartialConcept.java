package fr.insee.rmes.model.concepts;

import fr.insee.rmes.model.AppendableLabel;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialConcept(String id, String label, String altLabels)  implements AppendableLabel<PartialConcept>, PartialConceptBuilder.With {
}