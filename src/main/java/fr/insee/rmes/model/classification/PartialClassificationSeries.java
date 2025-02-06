package fr.insee.rmes.model.classification;

import fr.insee.rmes.model.AppendableLabel;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialClassificationSeries(String id, String label, String altLabels) implements AppendableLabel<PartialClassificationSeries>, PartialClassificationSeriesBuilder.With {
}
