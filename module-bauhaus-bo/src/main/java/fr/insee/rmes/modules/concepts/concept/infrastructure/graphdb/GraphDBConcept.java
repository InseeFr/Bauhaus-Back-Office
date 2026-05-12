package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptVersion;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GraphDBConcept(
        String id,
        @Nullable String prefLabelLg1,
        @Nullable String prefLabelLg2,
        @Nullable String creator,
        @Nullable String contributor,
        @Nullable String disseminationStatus,
        @Nullable String additionalMaterial,
        @Nullable String created,
        @Nullable String modified,
        @Nullable String valid,
        int conceptVersion,
        @Nullable String isValidated,
        List<String> altLabelLg1,
        List<String> altLabelLg2,
        List<String> collectionIds
) {

    public GraphDBConcept {
        altLabelLg1 = (altLabelLg1 == null) ? List.of() : altLabelLg1;
        altLabelLg2 = (altLabelLg2 == null) ? List.of() : altLabelLg2;
        collectionIds = (collectionIds == null) ? List.of() : collectionIds;
    }

    Concept toDomain() {
        return new Concept(
                new ConceptId(id),
                buildLabels(),
                Objects.requireNonNullElse(creator, ""),
                contributor,
                Objects.requireNonNullElse(disseminationStatus, ""),
                parseDateTime(Objects.requireNonNullElse(created, LocalDateTime.now().toString())),
                modified == null ? null : parseDateTime(modified),
                "true".equalsIgnoreCase(isValidated),
                new ConceptVersion(Math.max(conceptVersion, 1)),
                collectionIds
        );
    }

    private List<LocalisedLabel> buildLabels() {
        var labels = new ArrayList<LocalisedLabel>();
        if (prefLabelLg1 != null) {
            labels.add(new LocalisedLabel(prefLabelLg1, Lang.defaultLanguage()));
        }
        if (prefLabelLg2 != null) {
            labels.add(new LocalisedLabel(prefLabelLg2, Lang.alternativeLanguage()));
        }
        return labels;
    }

    private static LocalDateTime parseDateTime(String dateString) {
        if (!dateString.contains("T")) {
            return LocalDate.parse(dateString).atStartOfDay();
        }
        try {
            return LocalDateTime.parse(dateString);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(dateString).toLocalDateTime();
        }
    }
}
