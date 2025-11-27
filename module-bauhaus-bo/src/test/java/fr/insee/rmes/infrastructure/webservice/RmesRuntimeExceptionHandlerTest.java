package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import fr.insee.rmes.modules.commons.webservice.RmesRuntimeExceptionHandler;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RmesRuntimeExceptionHandlerTest {

    @Test
    public void shouldReturnMessageWhenHandleBadRequestException(){
        RmesRuntimeBadRequestException  rmesRuntimeBadRequestException = new RmesRuntimeBadRequestException("mockedMessage");
        RmesRuntimeExceptionHandler rmesRuntimeExceptionHandler = new RmesRuntimeExceptionHandler();
        String message = rmesRuntimeExceptionHandler.handleBadRequestException(rmesRuntimeBadRequestException).toString();
        assertTrue(message.startsWith("<400 BAD_REQUEST Bad Request,"));
    }
}