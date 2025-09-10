package fr.insee.rmes.model.operations;

import fr.insee.rmes.utils.DiacriticSorter;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialOperationIndicator(String id, String label,
                                        String altLabel) implements DiacriticSorter.AppendableLabel<PartialOperationIndicator>, PartialOperationIndicatorBuilder.With {
}
