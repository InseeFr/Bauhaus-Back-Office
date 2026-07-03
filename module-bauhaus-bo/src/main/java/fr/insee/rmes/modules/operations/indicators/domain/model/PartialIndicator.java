package fr.insee.rmes.modules.operations.indicators.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

import org.jspecify.annotations.Nullable;

public record PartialIndicator(IndicatorId id, LocalisedLabel prefLabel, @Nullable LocalisedLabel altLabel) {
}
