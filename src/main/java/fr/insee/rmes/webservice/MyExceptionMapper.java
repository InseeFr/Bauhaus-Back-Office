package fr.insee.rmes.webservice;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MyExceptionMapper implements ExceptionMapper<Exception> {
	
	@Override
	public Response toResponse(Exception arg0) {
		// TODO Auto-generated method stub
		return Response.status(500).entity(arg0.getMessage()).type(MediaType.TEXT_HTML).build();
	}

}
