package fr.insee.rmes.modules.operations.indicators.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

public record IndicatorWithSims(LocalisedLabel prefLabel, String idSims) {
}
