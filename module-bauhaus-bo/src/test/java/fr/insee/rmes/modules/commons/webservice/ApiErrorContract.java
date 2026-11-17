package fr.insee.rmes.modules.commons.webservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Contrat des réponses d'erreur de l'API (ADR-1264) :
 * {@code {message, code?, params?, errors?}}, en JSON.
 * <ul>
 *   <li>{@code message} : chaîne non vide ;</li>
 *   <li>{@code code} : chaîne non vide, jamais un nombre ;</li>
 *   <li>{@code params} : objet dont toutes les valeurs sont des chaînes ;</li>
 *   <li>{@code errors} : liste non vide de {@code {field, message}} ;</li>
 *   <li>aucun autre champ ({@code details}, {@code detail}, {@code status}...).</li>
 * </ul>
 */
public final class ApiErrorContract {

    private static final Set<String> ALLOWED_KEYS = Set.of("message", "code", "params", "errors");

    private ApiErrorContract() {}

    /** Réponse MockMvc conforme au contrat : type JSON et corps {@link #assertApiError}. */
    public static ResultMatcher apiError() {
        return result -> {
            String contentType = result.getResponse().getContentType();
            assertThat(contentType).as("Content-Type").isNotNull();
            assertThat(MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.APPLICATION_JSON))
                    .as("Content-Type %s", contentType)
                    .isTrue();
            assertApiError(result.getResponse().getContentAsString());
        };
    }

    public static void assertApiError(String body) {
        assertThat(body).as("corps d'erreur").isNotBlank();
        JSONObject error = new JSONObject(body);

        assertThat(ALLOWED_KEYS).as("champs du corps %s", body).containsAll(error.keySet());
        assertThat(error.opt("message")).as("message").isInstanceOf(String.class);
        assertThat(error.getString("message")).as("message").isNotBlank();

        if (error.has("code")) {
            assertThat(error.get("code")).as("code").isInstanceOf(String.class);
            assertThat(error.getString("code")).as("code").isNotBlank();
        }
        if (error.has("params")) {
            JSONObject params = error.getJSONObject("params");
            params.keySet()
                    .forEach(key ->
                            assertThat(params.get(key)).as("params.%s", key).isInstanceOf(String.class));
        }
        if (error.has("errors")) {
            JSONArray errors = error.getJSONArray("errors");
            assertThat(errors.length()).as("errors").isPositive();
            for (int i = 0; i < errors.length(); i++) {
                JSONObject fieldError = errors.getJSONObject(i);
                assertThat(fieldError.keySet()).as("errors[%d]", i).containsExactlyInAnyOrder("field", "message");
                assertThat(fieldError.get("field")).as("errors[%d].field", i).isInstanceOf(String.class);
                assertThat(fieldError.getString("message"))
                        .as("errors[%d].message", i)
                        .isNotBlank();
            }
        }
    }
}
