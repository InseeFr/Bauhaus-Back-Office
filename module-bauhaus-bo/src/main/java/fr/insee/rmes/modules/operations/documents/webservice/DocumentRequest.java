package fr.insee.rmes.modules.operations.documents.webservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

/**
 * Corps envoyé par l'IHM pour un document ou un lien : l'objet tel qu'elle l'affiche, dont seuls ces
 * champs comptent ({@code id}, {@code uri}, {@code sims}… sont ignorés).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentRequest(
        @Nullable String labelLg1,
        @Nullable String labelLg2,
        @Nullable String descriptionLg1,
        @Nullable String descriptionLg2,
        @Nullable String updatedDate,
        @Nullable String lang,
        @Nullable String url) {

    /** Le champ {@code body} d'un envoi multipart : le même objet, sérialisé en JSON. */
    static DocumentRequest fromJson(String json) throws RmesBadRequestException {
        JSONObject body;
        try {
            body = new JSONObject(json);
        } catch (JSONException _) {
            throw new RmesBadRequestException("The body is not a JSON object", json);
        }
        return new DocumentRequest(
                text(body, "labelLg1"),
                text(body, "labelLg2"),
                text(body, "descriptionLg1"),
                text(body, "descriptionLg2"),
                text(body, "updatedDate"),
                text(body, "lang"),
                text(body, "url"));
    }

    DocumentForm toForm() {
        return new DocumentForm(labelLg1, labelLg2, descriptionLg1, descriptionLg2, updatedDate, lang, url);
    }

    private static @Nullable String text(JSONObject body, String field) {
        return body.isNull(field) ? null : body.get(field).toString();
    }
}
