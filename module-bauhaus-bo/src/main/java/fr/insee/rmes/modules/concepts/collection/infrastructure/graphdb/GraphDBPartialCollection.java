package fr.insee.rmes.modules.concepts.collection.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collection.domain.model.CollectionId;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collection.domain.model.PartialCollection;

import java.util.UUID;

public record GraphDBPartialCollection(String id, String label) {
    PartialCollection toDomain(){
        return new PartialCollection(new CollectionId(id), LocalisedLabel.ofDefaultLanguage(label));
    }
}
