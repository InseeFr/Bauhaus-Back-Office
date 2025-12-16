package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.UpdateCollectionCommand;

import java.time.LocalDateTime;
import java.util.List;

public class UpdateCollectionRequest extends CreateCollectionRequest {
    private final String id;
    private final LocalDateTime created;

    public UpdateCollectionRequest(String id, List<LocalisedLabelResponse> labels, List<LocalisedLabelResponse> descriptions, String creator, String contributor, List<String> conceptsIdentifiers, LocalDateTime created) {
        super(labels, descriptions, creator, contributor, conceptsIdentifiers);
        this.id = id;
        this.created = created;
    }

    UpdateCollectionCommand toUpdateCommand() throws InvalidCreateCollectionCommandException, InvalidCollectionIdException {
        return new UpdateCollectionCommand(
                this.id,
                this.labels.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.descriptions.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.creator(),
                this.contributor(),
                this.conceptsIdentifiers(),
                this.created
        );
    }

    public String id() {
        return id;
    }
}
