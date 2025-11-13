package fr.insee.rmes.modules.classifications.nomenclatures.model;

import fr.insee.rmes.utils.DiacriticSorter;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PartialClassification(String id, String label,
                                    String altLabels) implements DiacriticSorter.AppendableLabels<PartialClassification>, PartialClassificationBuilder.With {

}
