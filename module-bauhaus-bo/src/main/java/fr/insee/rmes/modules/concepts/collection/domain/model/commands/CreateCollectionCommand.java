package fr.insee.rmes.modules.concepts.collection.domain.model.commands;

import fr.insee.rmes.modules.concepts.collection.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
//TODO RENAME alternativeLabel into secondLabel everywhere

public class CreateCollectionCommand {
    private final LocalisedLabel defaultLabel;
    private final @Nullable LocalisedLabel alternativeLabel;
    private final Map<Lang, String> descriptions;
    private final String creator;
    private final @Nullable String contributor;
    private final List<String> conceptsIdentifiers;

    public CreateCollectionCommand(LocalisedLabel defaultLabel, @Nullable LocalisedLabel alternativeLabel, Map<Lang, String> descriptions, String creator, @Nullable String contributor, List<String> conceptsIdentifiers) throws InvalidCreateCollectionCommandException {
        if(StringUtils.isAllBlank(creator)){
            throw new InvalidCreateCollectionCommandException("The creator is blank");
        }
        if(conceptsIdentifiers.stream().anyMatch(StringUtils::isAllBlank)){
            throw new InvalidCreateCollectionCommandException("At least one concept is blank");
        }

        this.defaultLabel = defaultLabel;
        this.alternativeLabel = alternativeLabel;
        this.descriptions = descriptions;
        this.creator = creator;
        this.contributor = contributor;
        this.conceptsIdentifiers = conceptsIdentifiers;
    }

    public LocalisedLabel defaultLabel() {
        return defaultLabel;
    }

    public Optional<LocalisedLabel> alternativeLabel() {
        return Optional.ofNullable(alternativeLabel);
    }

    public Map<Lang, String> descriptions() {
        return descriptions;
    }

    public String creator() {
        return creator;
    }

    public Optional<String> contributor() {
        return Optional.ofNullable(contributor);
    }

    public List<String> conceptsIdendifiers() {
        return conceptsIdentifiers;
    }


}


