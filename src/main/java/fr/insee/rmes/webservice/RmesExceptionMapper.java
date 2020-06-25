package fr.insee.rmes.webservice;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import fr.insee.rmes.exceptions.RestMessage;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;

@Provider
public class RmesExceptionMapper implements ExceptionMapper<Exception> {
    @Override
	public Response toResponse(Exception e) {
    	if (e.getClass().equals(AccessDeniedException.class)) {
			return Response.status(403)
                    .build();
		}
    	
    	if (e.getClass().equals(RmesUnauthorizedException.class)) {
    		RestMessage message = ((RmesUnauthorizedException) e).toRestMessage();
            return Response.status(message.getStatus())
                    .build();
    	}

    	if (e.getClass().equals(RmesException.class)) {
    		RmesException re = (RmesException) e;
            return Response.status(re.getStatus()).entity(re.getDetails()).type(MediaType.TEXT_PLAIN)
                    .build();
    	}
    	
        return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
                .build();
    }
}

