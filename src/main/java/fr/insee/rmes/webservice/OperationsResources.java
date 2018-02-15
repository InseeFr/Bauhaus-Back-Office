package fr.insee.rmes.webservice;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.operations.OperationsContract;
import fr.insee.rmes.persistance.service.sesame.operations.SesameOperationsImpl;
import fr.insee.rmes.persistance.service.sesame.operations.pojo.SerieForList;

@Component
@Path("/operations")
public class OperationsResources {
	
	final static Logger logger = LogManager.getLogger(OperationsResources.class);
	
	// TODO operationsContract with Spring
//	@Autowired 
//	OperationsContract operationsContract;
	
	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSeries() throws Exception {
		try {
			// TODO operationsContract with Spring
			OperationsContract operationsContract = new SesameOperationsImpl();
			List<SerieForList> series = operationsContract.getSeries();
			return Response.ok().entity(series).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

}
