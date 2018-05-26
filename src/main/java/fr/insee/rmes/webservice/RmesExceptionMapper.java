package fr.insee.rmes.webservice;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.security.access.AccessDeniedException;

import fr.insee.rmes.exceptions.RestMessage;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;

@Provider
public class RmesExceptionMapper implements ExceptionMapper<Exception> {
    public Response toResponse(Exception e) {
    	if (e.getClass().equals(AccessDeniedException.class))
            return Response.status(403)
                    .build();
    	
    	if (e.getClass().equals(RmesUnauthorizedException.class)) {
    		RestMessage message = ((RmesUnauthorizedException) e).toRestMessage();
            return Response.status(message.getStatus())
                    .build();
    	}
    	
        return Response.status(500)
                .build();
    }
}

