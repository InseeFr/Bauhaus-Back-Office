package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import fr.insee.rmes.persistance.service.ConceptsService;

/**
 * WebService class for resources of Concepts
 * 
 * 
 * @author N. Laval
 * 
 *         schemes: - http
 * 
 *         consumes: - application/json
 * 
 *         produces: - application/json
 *
 */
@Path("/concepts")
public class ConceptsPublicResources {
	
	@GET
	@Path("/concepts")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConcepts() {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getConcepts();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/concepts/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptsSearch() {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getConceptsSearch();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/concept/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptByID(@PathParam("id") String id) {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getConceptByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/concepts/toValidate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptsToValidate() {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getConceptsToValidate();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/concept/{id}/links")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptLinksByID(@PathParam("id") String id) {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getConceptLinksByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/concept/{id}/notes/{conceptVersion}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptNotesByID(@PathParam("id") String id, @PathParam("conceptVersion") int conceptVersion) {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getConceptNotesByID(id, conceptVersion);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/collections")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollections() {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getCollections();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/collections/dashboard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionsDashboard() {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getCollectionsDashboard();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/collections/toValidate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionsToValidate() {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getCollectionsToValidate();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/collection/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionByID(@PathParam("id") String id) {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getCollectionByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/collection/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionMembersByID(@PathParam("id") String id) {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.getCollectionMembersByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	

	@POST
	@Path("/private/concept")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setConcept(String body) {
		ConceptsService service = new ConceptsService();
		String jsonResultat = service.setConcept(body);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@PUT
	@Path("/private/concept/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConcept(@PathParam("id") String id, String body) {
		ConceptsService service = new ConceptsService();
		service.setConcept(id, body);
	}
	
	@PUT
	@Path("/private/concepts/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConceptsValidation(String body) {
		ConceptsService service = new ConceptsService();
		service.setConceptsValidation(body);
	}
	
	@GET
	@Path("/concept/export/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getConceptExport(@PathParam("id") String id) {
		ConceptsService service = new ConceptsService();
		return service.getConceptExport(id);
	}
	
	@POST
	@Path("/private/concept/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean setConceptSend(@PathParam("id") String id, String body) {
		ConceptsService service = new ConceptsService();
		return service.setConceptSend(id, body);
	}
	
	@POST
	@Path("/private/collection")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setCollection(String body) {
		ConceptsService service = new ConceptsService();
		service.setCollection(body);
	}
	
	@PUT
	@Path("/private/collection/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setCollection(@PathParam("id") String id, String body) {
		ConceptsService service = new ConceptsService();
		service.setCollection(id, body);
	}
	
	@PUT
	@Path("/private/collections/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setCollectionsValidation(String body) {
		ConceptsService service = new ConceptsService();
		service.setCollectionsValidation(body);
	}

	@GET
	@Path("/collection/export/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getCollectionExport(@PathParam("id") String id) {
		ConceptsService service = new ConceptsService();
		return service.getCollectionExport(id);
	}
	
	@POST
	@Path("/private/collection/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean setCollectionSend(@PathParam("id") String id, String body) {
		ConceptsService service = new ConceptsService();
		return service.setCollectionSend(id, body);
	}
}
