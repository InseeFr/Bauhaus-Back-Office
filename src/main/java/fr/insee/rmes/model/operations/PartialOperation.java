package fr.insee.rmes.model.operations;

import fr.insee.rmes.model.AppendableLabel;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialOperation(String id, String label, String iri, String seriesIri, String altLabels) implements AppendableLabel<PartialOperation>, PartialOperationBuilder.With {
}
