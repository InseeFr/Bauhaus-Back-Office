package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;

public record GraphDBConcept(String id, String prefLabelLg1, String prefLabelLg2) {
    CollectionMember toDomain() {
        return new CollectionMember(id, prefLabelLg1, prefLabelLg2);
    }
}
