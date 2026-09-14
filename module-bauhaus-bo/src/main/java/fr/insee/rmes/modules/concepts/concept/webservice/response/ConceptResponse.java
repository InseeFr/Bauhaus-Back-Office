package fr.insee.rmes.modules.concepts.concept.webservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.time.LocalDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConceptResponse(
        String id,
        String prefLabelLg1,
        @Nullable String prefLabelLg2,
        String creator,
        @Nullable String contributor,
        String disseminationStatus,
        @Nullable String additionalMaterial,
        String created,
        @Nullable String modified,
        @Nullable String valid,
        int conceptVersion,
        String validationState,
        @Nullable List<String> altLabelLg1,
        @Nullable List<String> altLabelLg2,
        List<String> collections) {

    public static ConceptResponse fromDomain(Concept concept) {
        String prefLabelLg2 = concept.alternativeLabels().stream()
                .filter(label -> label.lang() == Lang.alternativeLanguage())
                .map(LocalisedLabel::value)
                .findFirst()
                .orElse(null);

        return new ConceptResponse(
                concept.id().value(),
                concept.prefLabel().value(),
                prefLabelLg2,
                concept.creator(),
                concept.contributor().orElse(null),
                concept.disseminationStatus(),
                null,
                concept.created().toString(),
                concept.modified().map(LocalDateTime::toString).orElse(null),
                null,
                concept.version().value(),
                concept.validationState().getValue(),
                null,
                null,
                concept.collectionIds());
    }
}
