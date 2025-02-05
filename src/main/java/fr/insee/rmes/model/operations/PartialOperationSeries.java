package fr.insee.rmes.model.operations;

import fr.insee.rmes.model.AppendableLabel;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialOperationSeries(String id, String iri, String label, String altLabels) implements AppendableLabel<PartialOperationSeries>, PartialOperationSeriesBuilder.With {
}
