package fr.insee.rmes.model.operations;

import fr.insee.rmes.utils.DiacriticSorter;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialOperation(String id, String label, String iri, String seriesIri,
                               String altLabel) implements DiacriticSorter.AppendableLabel<PartialOperation>, PartialOperationBuilder.With {
}
