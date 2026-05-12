package fr.insee.rmes.modules.concepts.concept.domain.model;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.MalformedConceptException;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.CreateConceptCommand;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Concept extends CompactConcept {

    private static final boolean DEFAULT_VALIDATION_STATE = false;

    private final List<LocalisedLabel> alternativeLabels;
    private final String creator;
    private final @Nullable String contributor;
    private final String disseminationStatus;
    private final LocalDateTime created;
    private final @Nullable LocalDateTime modified;
    private final boolean isValidated;
    private final ConceptVersion version;
    private final List<String> collectionIds;

    public Concept(
            ConceptId id,
            List<LocalisedLabel> labels,
            String creator,
            @Nullable String contributor,
            String disseminationStatus,
            LocalDateTime created,
            @Nullable LocalDateTime modified,
            boolean isValidated,
            ConceptVersion version,
            List<String> collectionIds
    ) {
        super(
                id,
                labels.stream()
                        .filter(l -> l.lang().equals(Lang.defaultLanguage()))
                        .findFirst()
                        .orElseThrow(() -> new MalformedConceptException(
                                "No label for the default language (" + Lang.defaultLanguage() + ")"))
        );
        this.alternativeLabels = labels.stream()
                .filter(l -> !l.lang().equals(Lang.defaultLanguage()))
                .toList();
        this.creator = creator;
        this.contributor = contributor;
        this.disseminationStatus = disseminationStatus;
        this.created = created;
        this.modified = modified;
        this.isValidated = isValidated;
        this.version = version;
        this.collectionIds = collectionIds;
    }

    public static Concept create(CreateConceptCommand command, ConceptId conceptId) {
        return new Concept(
                conceptId,
                command.labels(),
                command.creator(),
                command.contributor().orElse(null),
                command.disseminationStatus(),
                LocalDateTime.now(),
                null,
                DEFAULT_VALIDATION_STATE,
                ConceptVersion.initial(),
                command.collectionIds()
        );
    }

    public List<LocalisedLabel> alternativeLabels() {
        return alternativeLabels;
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

    public LocalDateTime created() {
        return created;
    }

    public Optional<LocalDateTime> modified() {
        return Optional.ofNullable(modified);
    }

    public boolean isValidated() {
        return isValidated;
    }

    public ConceptVersion version() {
        return version;
    }

    public List<String> collectionIds() {
        return collectionIds;
    }
}
