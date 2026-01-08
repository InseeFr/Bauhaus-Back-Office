package fr.insee.rmes.modules.concepts.collections.domain.model.commands;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CreateCollectionCommand {
    private final List<LocalisedLabel> labels;
    private final List<LocalisedLabel> descriptions;
    private final String creator;
    private final @Nullable String contributor;
    private final List<String> conceptsIdentifiers;

    public CreateCollectionCommand(List<LocalisedLabel> labels, List<LocalisedLabel> descriptions, String creator, @Nullable String contributor, List<String> conceptsIdentifiers) throws InvalidCreateCollectionCommandException {
        if(labels.isEmpty()){
            throw new InvalidCreateCollectionCommandException("There are no labels");
        }
        if(labels.stream().noneMatch(l -> l.lang().equals(Lang.defaultLanguage()))){
            throw new InvalidCreateCollectionCommandException("The default label is not provided");
        }
        if(StringUtils.isAllBlank(creator)){
            throw new InvalidCreateCollectionCommandException("The creator is blank");
        }
        if(conceptsIdentifiers.stream().anyMatch(StringUtils::isAllBlank)){
            throw new InvalidCreateCollectionCommandException("At least one concept is blank");
        }

        this.labels = labels;
        this.descriptions = descriptions;
        this.creator = creator;
        this.contributor = contributor;
        this.conceptsIdentifiers = conceptsIdentifiers;
    }

    public List<LocalisedLabel> labels() {
        return labels;
    }

    public List<LocalisedLabel> descriptions() {
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


