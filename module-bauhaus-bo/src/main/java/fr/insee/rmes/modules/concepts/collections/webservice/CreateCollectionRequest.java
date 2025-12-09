package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;

import java.util.List;

public class CreateCollectionRequest {

    protected final List<LocalisedLabelResponse> labels;
    protected final List<LocalisedLabelResponse> descriptions;
    protected final String creator;
    protected final String contributor;
    protected final List<String> conceptsIdentifiers;

    public CreateCollectionRequest(List<LocalisedLabelResponse> labels, List<LocalisedLabelResponse> descriptions, String creator, String contributor, List<String> conceptsIdentifiers) {
        this.labels = labels;
        this.descriptions = descriptions;
        this.creator = creator;
        this.contributor = contributor;
        this.conceptsIdentifiers = conceptsIdentifiers;
    }

    CreateCollectionCommand toCreateCommand() throws InvalidCreateCollectionCommandException {
        return new CreateCollectionCommand(
                this.labels.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.descriptions.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.creator,
                this.contributor,
                this.conceptsIdentifiers
        );
    }

    public List<LocalisedLabelResponse> labels() {
        return labels;
    }

    public List<LocalisedLabelResponse> descriptions() {
        return descriptions;
    }

    public String creator() {
        return creator;
    }

    public String contributor() {
        return contributor;
    }

    public List<String> conceptsIdentifiers() {
        return conceptsIdentifiers;
    }
}
