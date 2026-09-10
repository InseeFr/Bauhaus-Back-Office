package fr.insee.rmes.modules.concepts.collections.domain.model.commands;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class UpdateCollectionCommand extends CreateCollectionCommand {
    private final CollectionId collectionId;

    public UpdateCollectionCommand(
            String id,
            List<LocalisedLabel> labels,
            List<LocalisedLabel> descriptions,
            String creator,
            @Nullable String contributor,
            List<String> conceptsIdentifiers)
            throws InvalidCreateCollectionCommandException, InvalidCollectionIdException {

        super(id, labels, descriptions, creator, contributor, conceptsIdentifiers);
        this.collectionId = new CollectionId(id);
    }

    public CollectionId collectionId() {
        return collectionId;
    }
}
