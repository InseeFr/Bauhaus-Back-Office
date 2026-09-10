package fr.insee.rmes.modules.commons.webservice;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.ValueInstantiationException;

/**
 * Format d'erreur contractuel de la validation des corps de requête, commun à tous les modules :
 * {@code 400} et {@code {"errors":[{"field","message"}]}}.
 * <p>
 * {@link Order}({@link Ordered#HIGHEST_PRECEDENCE}) est indispensable : {@code RmesExceptionHandler}
 * est déclaré {@code @Order(2)} sur une liste {@code assignableTypes} qui couvre presque tous les
 * contrôleurs d'écriture, et rendrait sinon le {@code ProblemDetail} de
 * {@code ResponseEntityExceptionHandler}.
 * <p>
 * Cette classe n'étend volontairement pas {@code ResponseEntityExceptionHandler} : c'est justement
 * le comportement devant lequel on veut passer.
 * <p>
 * Les exceptions Jackson visées sont celles de <strong>Jackson 3</strong>
 * ({@code tools.jackson}), que Spring Boot 4 utilise pour ses convertisseurs de message. Jackson 2
 * ({@code com.fasterxml}) reste au classpath pour d'autres usages, mais ne remonte pas d'un
 * {@code @RequestBody}.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler {

    /** Erreur portant sur le corps entier, faute de champ identifiable. */
    static final String WHOLE_BODY = "body";

    private static final String UNREADABLE_BODY = "the request body could not be read";

    public record ValidationError(String field, String message) {}

    public record ValidationErrors(List<ValidationError> errors) {}

    /** Contrainte Bean Validation violée sur un {@code @Valid @RequestBody}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrors> handleInvalidArgument(MethodArgumentNotValidException exception) {
        List<ValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ValidationError(fieldError.getField(), messageOf(fieldError)))
                .toList();
        return ResponseEntity.badRequest().body(new ValidationErrors(errors));
    }

    /**
     * Corps que Jackson ne sait pas lire : JSON malformé, valeur du mauvais type, ou constructeur
     * du DTO qui refuse la charge utile. Sans cette branche, le typage des champs rendrait un 400
     * hors contrat.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrors> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new ValidationErrors(List.of(errorOf(exception.getCause()))));
    }

    private static String messageOf(FieldError fieldError) {
        return Objects.requireNonNullElse(fieldError.getDefaultMessage(), "is invalid");
    }

    private static ValidationError errorOf(Throwable cause) {
        // Le DTO s'est refusé lui-même (contrôle dans le constructeur) : son message est le plus parlant.
        if (cause instanceof ValueInstantiationException valueInstantiation) {
            return new ValidationError(fieldOf(valueInstantiation), rootMessageOf(valueInstantiation));
        }
        if (cause instanceof MismatchedInputException mismatchedInput) {
            return new ValidationError(fieldOf(mismatchedInput), typeMismatchMessageOf(mismatchedInput));
        }
        // JSON malformé : le message de Jackson expose l'état du parseur, on ne le relaie pas.
        return new ValidationError(WHOLE_BODY, UNREADABLE_BODY);
    }

    /** {@code getPath()} est porté par {@link JacksonException}, parent commun des deux cas traités. */
    private static String fieldOf(JacksonException exception) {
        String path = exception.getPath().stream()
                .map(JacksonException.Reference::getPropertyName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
        return path.isEmpty() ? WHOLE_BODY : path;
    }

    private static String typeMismatchMessageOf(MismatchedInputException exception) {
        Class<?> targetType = exception.getTargetType();
        return targetType == null ? UNREADABLE_BODY : "is not a valid " + targetType.getSimpleName();
    }

    private static String rootMessageOf(ValueInstantiationException exception) {
        Throwable cause = exception.getCause();
        return cause != null && cause.getMessage() != null ? cause.getMessage() : UNREADABLE_BODY;
    }
}
