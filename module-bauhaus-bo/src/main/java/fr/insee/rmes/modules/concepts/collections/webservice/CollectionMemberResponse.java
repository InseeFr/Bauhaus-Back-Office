package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import org.jspecify.annotations.Nullable;

public record CollectionMemberResponse(String id, String prefLabelLg1, @Nullable String prefLabelLg2) {
    static CollectionMemberResponse fromDomain(CollectionMember member) {
        return new CollectionMemberResponse(member.id(), member.prefLabelLg1(), member.prefLabelLg2());
    }
}
