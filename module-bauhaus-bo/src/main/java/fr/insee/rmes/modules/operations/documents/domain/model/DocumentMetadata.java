package fr.insee.rmes.modules.operations.documents.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Métadonnées d'un document ou d'un lien telles qu'elles sont stockées.
 *
 * @param size taille du fichier ({@code dct:extent}, en octets) ; absente pour un lien ou un document pas
 *     encore repris
 */
public record DocumentMetadata(
        String uri,
        List<LocalisedLabel> labels,
        List<LocalisedLabel> comments,
        @Nullable LocalDate updatedDate,
        @Nullable String language,
        @Nullable FileSize size,
        String url) {}
