package fr.insee.rmes.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.rmes.domain.exceptions.RmesException;
import java.nio.file.NoSuchFileException;
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

        ResponseEntity<String> actual = rmesExceptionHandler.handleRmesException(rmesException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 403, 404, 409, 500, 503})
    void an_error_status_is_kept(int status) {
        RmesException rmesException = new RmesException(status, "RmesException message", "RmesExceptionDetails");

        ResponseEntity<String> actual = rmesExceptionHandler.handleRmesException(rmesException);

        assertEquals(status, actual.getStatusCode().value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fileOne-otherOne-reasonOne", "fileTwo-otherTwo-reasonTwo"})
    void shouldReturnHandleRmesExceptionFromNoSuchFileException(String details) {
        String[] infos = details.split("-");
        NoSuchFileException noSuchFileException = new NoSuchFileException(infos[0], infos[1], infos[2]);
        ResponseEntity<String> actual = rmesExceptionHandler.handleRmesException(noSuchFileException);
        String expected =
                "<404 NOT_FOUND Not Found," + infos[0] + " -> " + infos[1] + ": " + infos[2] + " does not exist,[]>";
        assertEquals(expected, actual.toString());
    }
}
