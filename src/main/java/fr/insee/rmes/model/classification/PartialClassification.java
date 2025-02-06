package fr.insee.rmes.model.classification;

import fr.insee.rmes.model.AppendableLabel;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialClassification(String id, String label, String altLabels) implements AppendableLabel<PartialClassification>, PartialClassificationBuilder.With {

}
