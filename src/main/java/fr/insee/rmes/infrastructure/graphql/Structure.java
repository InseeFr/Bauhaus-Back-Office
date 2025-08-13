package fr.insee.rmes.infrastructure.graphql;

import java.time.LocalDateTime;
import java.util.List;

record Component(
        LocalDateTime created,
        String identifiant,
        String type,
        List<String> contributor,
        String labelLg2,
        String labelLg1,
        LocalDateTime modified,
        String id,
        String disseminationStatus,
        String validationState
) {}

record ComponentDefinition(
        Component component,
        List<String> attachment,
        LocalDateTime created,
        LocalDateTime modified,
        String id,
        boolean required,
        String order
) {}

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
        List<ComponentDefinition> componentDefinitions,
        String validationState
) {}