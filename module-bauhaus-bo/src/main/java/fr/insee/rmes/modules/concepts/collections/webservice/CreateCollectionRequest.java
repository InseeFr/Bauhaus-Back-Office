package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import java.util.List;
import java.util.regex.Pattern;

public class CreateCollectionRequest {

    private static final Pattern ID_PATTERN = Pattern.compile(CollectionId.VALID_PATTERN);

    protected final String id;
    protected final List<LocalisedLabelResponse> labels;
    protected final List<LocalisedLabelResponse> descriptions;
    protected final String creator;
    protected final String contributor;
    protected final List<String> conceptsIdentifiers;

    public CreateCollectionRequest(
            String id,
            List<LocalisedLabelResponse> labels,
            List<LocalisedLabelResponse> descriptions,
            String creator,
            String contributor,
            List<String> conceptsIdentifiers) {
        this.id = id;
        this.labels = labels;
        this.descriptions = descriptions;
        this.creator = creator;
        this.contributor = contributor;
        this.conceptsIdentifiers = conceptsIdentifiers;
    }

    CreateCollectionCommand toCreateCommand() throws InvalidCreateCollectionCommandException {
        if (this.id != null
                && !this.id.isEmpty()
                && !ID_PATTERN.matcher(this.id).matches()) {
            throw new InvalidCreateCollectionCommandException(
                    "The identifier is invalid: only alphanumeric characters and hyphens are allowed");
        }
        return new CreateCollectionCommand(
                this.id,
                this.labels.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.descriptions.stream().map(LocalisedLabelResponse::toDomain).toList(),
                this.creator,
                this.contributor,
                this.conceptsIdentifiers);
    }

    public String id() {
        return id;
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
