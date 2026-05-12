package fr.insee.rmes.modules.concepts.concept.domain.model.commands;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidConceptIdException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class UpdateConceptCommand extends CreateConceptCommand {
    private final ConceptId conceptId;

    public UpdateConceptCommand(
            String id,
            List<LocalisedLabel> labels,
            String creator,
            @Nullable String contributor,
            String disseminationStatus,
            List<String> collectionIds
    ) throws InvalidCreateConceptCommandException, InvalidConceptIdException {
        super(labels, creator, contributor, disseminationStatus, collectionIds);
        this.conceptId = new ConceptId(id);
    }

    public ConceptId conceptId() {
        return conceptId;
    }
}
