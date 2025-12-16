package fr.insee.rmes.modules.concepts.collections.domain.model.commands;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidUpdateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public class UpdateCollectionCommand extends CreateCollectionCommand{
    private final CollectionId id;
    private final LocalDateTime created;

    public UpdateCollectionCommand(String id, List<LocalisedLabel> labels, List<LocalisedLabel> descriptions, String creator, @Nullable String contributor, List<String> conceptsIdentifiers, LocalDateTime created) throws InvalidCreateCollectionCommandException, InvalidCollectionIdException {

        super(labels, descriptions, creator, contributor, conceptsIdentifiers);
        this.id = new CollectionId(id);
        if(created == null){
            throw new InvalidUpdateCollectionCommandException("provided creation date is null");
        }
        this.created = created;
    }

    public CollectionId id() {
        return id;
    }

    public LocalDateTime created(){
        return created;
    }
}


