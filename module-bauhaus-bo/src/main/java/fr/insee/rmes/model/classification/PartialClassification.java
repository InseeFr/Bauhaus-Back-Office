package fr.insee.rmes.model.classification;

import fr.insee.rmes.infrastructure.utils.DiacriticSorter;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialClassification(String id, String label,
                                    String altLabels) implements DiacriticSorter.AppendableLabels<PartialClassification>, PartialClassificationBuilder.With {

}
