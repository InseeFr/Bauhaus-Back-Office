package fr.insee.rmes.modules.concepts.collections.domain.model;

import org.jspecify.annotations.Nullable;

public record CollectionMember(
        String id,
        String prefLabelLg1,
        @Nullable String prefLabelLg2
) {
}
