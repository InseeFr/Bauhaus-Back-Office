package fr.insee.rmes.modules.concepts.concept.domain.model.commands;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class CreateConceptCommand {
    private final List<LocalisedLabel> labels;
    private final String creator;
    private final @Nullable String contributor;
    private final String disseminationStatus;
    private final List<String> collectionIds;

    public CreateConceptCommand(
            List<LocalisedLabel> labels,
            String creator,
            @Nullable String contributor,
            String disseminationStatus,
            List<String> collectionIds)
            throws InvalidCreateConceptCommandException {
        if (labels.isEmpty()) {
            throw new InvalidCreateConceptCommandException("There are no labels");
        }
        if (labels.stream().noneMatch(l -> l.lang().equals(Lang.defaultLanguage()))) {
            throw new InvalidCreateConceptCommandException("The default label is not provided");
        }
        if (StringUtils.isAllBlank(creator)) {
            throw new InvalidCreateConceptCommandException("The creator is blank");
        }
        if (StringUtils.isAllBlank(disseminationStatus)) {
            throw new InvalidCreateConceptCommandException("The dissemination status is blank");
        }
        if (collectionIds.stream().anyMatch(StringUtils::isAllBlank)) {
            throw new InvalidCreateConceptCommandException("At least one collection identifier is blank");
        }

        this.labels = labels;
        this.creator = creator;
        this.contributor = contributor;
        this.disseminationStatus = disseminationStatus;
        this.collectionIds = collectionIds;
    }

    public List<LocalisedLabel> labels() {
        return labels;
    }

    public String creator() {
        return creator;
    }

    public Optional<String> contributor() {
        return Optional.ofNullable(contributor);
    }

    public String disseminationStatus() {
        return disseminationStatus;
    }

    public List<String> collectionIds() {
        return collectionIds;
    }
}
