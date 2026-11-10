package fr.insee.rmes.modules.operations.documents.webservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentDescription;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentType;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/** Description d'un document ou d'un lien, dans le format des rapports qualité de Magma. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record DocumentDescriptionResponse(
        String uri,
        List<LocalisedContentResponse> label,
        List<LocalisedContentResponse> commentaire,
        @Nullable String dateMiseAJour,
        @Nullable String langue,
        String taille,
        DocumentType type,
        String url) {

    public record LocalisedContentResponse(String contenu, String langue) {

        static LocalisedContentResponse fromDomain(LocalisedLabel label) {
            return new LocalisedContentResponse(
                    label.value(), label.lang().name().toLowerCase(Locale.ROOT));
        }
    }

    public static DocumentDescriptionResponse fromDomain(DocumentDescription description) {
        DocumentMetadata metadata = description.metadata();
        LocalDate updatedDate = metadata.updatedDate();
        return new DocumentDescriptionResponse(
                metadata.uri(),
                metadata.labels().stream()
                        .map(LocalisedContentResponse::fromDomain)
                        .toList(),
                metadata.comments().stream()
                        .map(LocalisedContentResponse::fromDomain)
                        .toList(),
                updatedDate == null ? null : updatedDate.toString(),
                metadata.language(),
                description.size().humanReadable(),
                description.type(),
                metadata.url());
    }
}
