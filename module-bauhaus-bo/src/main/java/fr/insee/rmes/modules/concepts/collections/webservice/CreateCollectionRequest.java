package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;

import java.util.List;

public record CreateCollectionRequest(
        List<LocalisedLabelResponse> labels,
        List<LocalisedLabelResponse> descriptions,
        String creator,
        String contributor,
        List<String> conceptsIdentifiers
) {
    CreateCollectionCommand toCommand() throws InvalidCreateCollectionCommandException {


        return new CreateCollectionCommand(
                this.labels.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.descriptions.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.creator,
                this.contributor,
                this.conceptsIdentifiers

        );
    }
}
