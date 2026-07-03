package fr.insee.rmes.modules.operations.indicators.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

import java.util.List;

public record OperationLink(String id, String type, List<LocalisedLabel> labels) {
}
