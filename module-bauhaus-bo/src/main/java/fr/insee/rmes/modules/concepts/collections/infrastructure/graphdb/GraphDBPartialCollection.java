package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.Lang;
import fr.insee.rmes.modules.concepts.collections.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;

public record GraphDBPartialCollection(String id, String label) {
    PartialCollection toDomain(){
        return new PartialCollection(new CollectionId(id), new LocalisedLabel(label, Lang.defaultLanguage()));
    }
}
