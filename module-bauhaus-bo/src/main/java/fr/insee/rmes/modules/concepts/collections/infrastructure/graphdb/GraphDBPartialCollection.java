package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;

public record GraphDBPartialCollection(String id, String label, String label_lg) {
    CompactCollection toDomain(){
        var localisedLabel = new LocalisedLabel(label, Lang.valueOf(label_lg.toUpperCase()));
        return new CompactCollection(new CollectionId(id), localisedLabel);
    }
}
