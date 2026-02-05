package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.UpdateCollectionCommand;

import java.util.List;

public class UpdateCollectionRequest extends CreateCollectionRequest {
    private final String id;

    public UpdateCollectionRequest(String id, List<LocalisedLabelResponse> labels, List<LocalisedLabelResponse> descriptions, String creator, String contributor, List<String> conceptsIdentifiers) {
        super(labels, descriptions, creator, contributor, conceptsIdentifiers);
        this.id = id;
    }

    UpdateCollectionCommand toUpdateCommand() throws InvalidCreateCollectionCommandException, InvalidCollectionIdException {
        return new UpdateCollectionCommand(
                this.id,
                this.labels.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.descriptions.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.creator(),
                this.contributor(),
                this.conceptsIdentifiers()
        );
    }

    public String id() {
        return id;
    }
}
