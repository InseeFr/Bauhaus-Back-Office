package fr.insee.rmes.model.operations;

import fr.insee.rmes.infrastructure.utils.DiacriticSorter;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialOperationSeries(String id, String iri, String label,
                                     String altLabel) implements DiacriticSorter.AppendableLabel<PartialOperationSeries>, PartialOperationSeriesBuilder.With {
}
