package fr.insee.rmes.modules.commons.configuration.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * Format unique des réponses d'erreur de l'API (ADR-1264), décrit dans le contrat OpenAPI : le schéma
 * {@value #API_ERROR} (miroir de {@code fr.insee.rmes.modules.commons.webservice.ApiError}) et une
 * réponse {@code default} qui y renvoie sur chaque opération. Tout statut 4xx/5xx d'une opération
 * porte ce corps, en {@code application/json}.
 */
public class ApiErrorOpenApiCustomizer implements OpenApiCustomizer {

    static final String API_ERROR = "ApiError";

    static final String FIELD_ERROR = "FieldError";

    private static final String COMPONENTS_SCHEMAS = "#/components/schemas/";

    private static final String MESSAGE = "message";

    private static final String FIELD = "field";

    @Override
    public void customise(OpenAPI openApi) {
        openApi.getComponents().addSchemas(FIELD_ERROR, fieldErrorSchema()).addSchemas(API_ERROR, apiErrorSchema());
        if (openApi.getPaths() == null) {
            return;
        }
        openApi.getPaths().values().stream()
                .map(PathItem::readOperations)
                .flatMap(List::stream)
                .forEach(ApiErrorOpenApiCustomizer::documentErrors);
    }

    private static void documentErrors(Operation operation) {
        if (operation.getResponses() == null) {
            operation.setResponses(new ApiResponses());
        }
        operation.getResponses().addApiResponse(ApiResponses.DEFAULT, errorResponse());
    }

    private static ApiResponse errorResponse() {
        return new ApiResponse()
                .description("Error (any 4xx or 5xx status)")
                .content(new Content()
                        .addMediaType(
                                // MediaType importé est celui d'OpenAPI : la constante est celle de Spring
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                new MediaType().schema(new Schema<>().$ref(COMPONENTS_SCHEMAS + API_ERROR))));
    }

    private static Schema<?> apiErrorSchema() {
        return new ObjectSchema()
                .description("Error body of every 4xx/5xx response")
                .addProperty(
                        MESSAGE,
                        new StringSchema().description("Readable fallback in English, never empty, never technical"))
                .addProperty(
                        "code", new StringSchema().description("Stable translation key of the error, e.g. \"804\""))
                .addProperty(
                        "params",
                        new MapSchema()
                                .additionalProperties(new StringSchema())
                                .description("Values to interpolate in the translation of the code"))
                .addProperty(
                        "errors",
                        new ArraySchema()
                                .items(new Schema<>().$ref(COMPONENTS_SCHEMAS + FIELD_ERROR))
                                .description("Validation errors (400 only), one per faulty field"))
                .required(List.of(MESSAGE));
    }

    private static Schema<?> fieldErrorSchema() {
        return new ObjectSchema()
                .addProperty(
                        FIELD,
                        new StringSchema()
                                .description("Dotted path of the field, or \"body\" for the whole request body"))
                .addProperty(MESSAGE, new StringSchema())
                .required(List.of(FIELD, MESSAGE));
    }
}
