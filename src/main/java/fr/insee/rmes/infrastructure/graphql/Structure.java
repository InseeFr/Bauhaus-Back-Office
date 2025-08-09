package fr.insee.rmes.infrastructure.graphql;

import java.time.LocalDateTime;
import java.util.List;

public record Structure(
        String id,
        String identifiant,
        String labelLg1,
        String labelLg2,
        String creator,
        List<String> contributor,
        LocalDateTime created,
        LocalDateTime modified,
        String disseminationStatus,
        List<String> componentDefinitions,
        String validationState
) {}