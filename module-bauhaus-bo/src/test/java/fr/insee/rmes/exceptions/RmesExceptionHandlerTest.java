package fr.insee.rmes.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.rmes.domain.exceptions.CodedRmesException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.webservice.ApiError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RmesExceptionHandlerTest {

    RmesExceptionHandler rmesExceptionHandler = new RmesExceptionHandler();

    @ParameterizedTest
    @ValueSource(ints = {0, 100, 200, 204, 302, 999})
    void a_status_which_is_not_an_error_answers_500(int status) {
        RmesException rmesException = new RmesException(status, "RmesException message", "RmesExceptionDetails");

        ResponseEntity<ApiError> actual = rmesExceptionHandler.handleRmesException(rmesException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 403, 404, 409, 500, 503})
    void an_error_status_is_kept(int status) {
        RmesException rmesException = new RmesException(status, "RmesException message", "RmesExceptionDetails");

        ResponseEntity<ApiError> actual = rmesExceptionHandler.handleRmesException(rmesException);

        assertEquals(status, actual.getStatusCode().value());
    }

    @Test
    void a_coded_exception_answers_its_status_and_its_code_without_the_technical_cause() {
        CodedRmesException exception = new CodedRmesException(
                503, "PUBLICATION_REPOSITORY_UNAVAILABLE", "Publication failed", new IllegalStateException("rdf4j"));

        ResponseEntity<ApiError> actual = rmesExceptionHandler.handleRmesException(exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, actual.getStatusCode());
        assertEquals(new ApiError("Publication failed", "PUBLICATION_REPOSITORY_UNAVAILABLE"), actual.getBody());
    }
}
