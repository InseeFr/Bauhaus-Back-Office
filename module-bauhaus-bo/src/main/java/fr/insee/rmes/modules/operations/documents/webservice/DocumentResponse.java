package fr.insee.rmes.modules.operations.documents.webservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentDetails;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.SimsReference;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Un document ou un lien tel que l'IHM le lit. Seuls les champs renseignés sont écrits, comme dans
 * la réponse historique ; {@code size} est la taille du fichier en octets.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentResponse(
        String id,
        String uri,
        @Nullable String url,
        @Nullable String labelLg1,
        @Nullable String labelLg2,
        @Nullable String descriptionLg1,
        @Nullable String descriptionLg2,
        @Nullable String updatedDate,
        @Nullable String lang,
        @Nullable Long size,
        @Nullable List<SimsResponse> sims) {

    /** Rubrique d'un rapport qualité qui cite le document, avec les timbres des propriétaires du rapport. */
    public record SimsResponse(
            String id,
            @Nullable String labelLg1,
            @Nullable String labelLg2,
            String simsRubricId,
            List<String> creators) {

        static SimsResponse fromDomain(SimsReference reference) {
            return new SimsResponse(
                    reference.simsId(),
                    reference.labelLg1(),
                    reference.labelLg2(),
                    reference.rubricId(),
                    reference.creators());
        }
    }

    static DocumentResponse fromDomain(ManagedDocument document) {
        return of(document, null);
    }

    static DocumentResponse fromDomain(DocumentDetails details) {
        return of(
                details.document(),
                details.sims().stream().map(SimsResponse::fromDomain).toList());
    }

    private static DocumentResponse of(ManagedDocument document, @Nullable List<SimsResponse> sims) {
        DocumentForm form = document.form();
        FileSize size = document.size();
        return new DocumentResponse(
                document.id(),
                document.uri(),
                form.url(),
                form.labelLg1(),
                form.labelLg2(),
                form.descriptionLg1(),
                form.descriptionLg2(),
                form.updatedDate(),
                form.lang(),
                size == null ? null : size.bytes(),
                sims);
    }
}
