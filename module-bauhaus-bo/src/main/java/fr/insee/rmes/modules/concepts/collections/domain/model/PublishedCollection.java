package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.MalformedCollectionException;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PublishedCollection extends CompactCollection {

    private static final boolean DEFAULT_VALIDATION_STATE = false;
    private final @Nullable List<LocalisedLabel> alternativeLabels;
    private final List<LocalisedLabel> descriptions;
    private final LocalDateTime created;
    private final @Nullable LocalDateTime modified;
    private final List<ConceptId> conceptIds;

    public PublishedCollection(CollectionId id,
                               List<LocalisedLabel> labels,
                               List<LocalisedLabel> descriptions,
                               LocalDateTime created,
                               @Nullable LocalDateTime modified,
                               List<ConceptId> conceptIds) {
        var prefLabel = labels.stream().filter(l -> l.lang().equals(Lang.defaultLanguage())).findFirst();
        var alternativeLabels = labels.stream().filter(l -> !l.lang().equals(Lang.defaultLanguage())).toList();



        super(id, prefLabel.orElseThrow(() -> new MalformedCollectionException("There is not label for default language")));
        this.alternativeLabels = alternativeLabels;
        this.descriptions = descriptions;
        this.created = created;
        this.modified = modified;
        this.conceptIds = conceptIds;
    }

    public static PublishedCollection fromCollection(Collection collection){
        List labels = new ArrayList(collection.alternativeLabels());
        labels.add(collection.prefLabel());

        return new PublishedCollection(
                collection.id(),
                labels,
                collection.descriptions(),
                collection.created(),
                collection.modified().orElse(collection.created()),
                collection.conceptIds()
        );
    }


    public List<LocalisedLabel> alternativeLabels() {
        return alternativeLabels;
    }

    public List<LocalisedLabel> descriptions() {
        return descriptions;
    }

    public LocalDateTime created() {
        return created;
    }

    public Optional<LocalDateTime> modified() {
        return Optional.ofNullable(modified);
    }

    public List<ConceptId> conceptIds() {
        return conceptIds;
    }
}
