package fr.insee.rmes.infrastructure.graphql;

import java.time.LocalDateTime;
import java.util.List;

public record Concept(
        String id,
        String creator,
        String prefLabelLg1,
        String prefLabelLg2,
        LocalDateTime created,
        String conceptVersion,
        LocalDateTime valid,
        String contributor,
        boolean isValidated,
        LocalDateTime modified,
        String disseminationStatus,
        List<String> altLabelLg1,
        List<String> altLabelLg2
) {}