package fr.insee.rmes.infrastructure.graphql;

import java.time.LocalDateTime;
import java.util.List;

record Keywords(
        List<String> lg1,
        List<String> lg2
) {}

record CatalogRecord(
        String creator,
        List<String> contributor,
        LocalDateTime created,
        LocalDateTime updated
) {}

