package fr.insee.rmes.modules.operations.documents.domain.model;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** Rubrique d'un rapport qualité qui cite un document ou un lien. */
public record SimsReference(
        String simsId, @Nullable String labelLg1, @Nullable String labelLg2, String rubricId, List<String> creators) {}
