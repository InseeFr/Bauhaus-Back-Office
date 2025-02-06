package fr.insee.rmes.model.operations;

import fr.insee.rmes.model.AppendableLabel;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialOperationIndicator(String id, String label, String altLabels) implements AppendableLabel<PartialOperationIndicator>, PartialOperationIndicatorBuilder.With {
}
