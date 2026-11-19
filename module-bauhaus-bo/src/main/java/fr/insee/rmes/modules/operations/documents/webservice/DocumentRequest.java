package fr.insee.rmes.modules.operations.documents.webservice;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

/**
 * Corps envoyé par l'IHM pour un document ou un lien : l'objet tel qu'elle l'affiche, dont seuls ces
 * champs comptent ({@code id}, {@code uri}, {@code sims}… sont ignorés).
 * <p>
 * Validé en modification ({@code @Valid @RequestBody}) comme en création, où il arrive en partie
 * JSON {@code body} d'un envoi multipart ({@code @Valid @RequestPart}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentRequest(
        @NotBlank(message = REQUIRED) @Nullable String labelLg1,
        @NotBlank(message = REQUIRED) @Nullable String labelLg2,
        @Nullable String descriptionLg1,
        @Nullable String descriptionLg2,
        @Nullable String updatedDate,
        @NotBlank(message = REQUIRED) @Nullable String lang,
        @Nullable String url) {

    DocumentForm toForm() {
        return new DocumentForm(labelLg1, labelLg2, descriptionLg1, descriptionLg2, updatedDate, lang, url);
    }
}
