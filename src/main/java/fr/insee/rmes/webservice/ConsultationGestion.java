package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.consutation_gestion.ConsultationGestionService;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Component
@Path("/consultation-gestion")
@Tag(name = "Consultation Gestion", description = "Consultation Gestion API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class ConsultationGestion {

    @Autowired
    ConsultationGestionService consultationGestionService;



    @GET()
    @Path("/concept/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getDetailedConcept", summary = "Get a concept")
    public Response getDetailedConcept(@PathParam(Constants.ID) String id) {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getDetailedConcept(id);
        } catch (RmesException e) {
            return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
    }

    @GET()
    @Path("/concepts")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllConcepts", summary = "Get all concepts")
    public Response getAllConcepts() {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getAllConcepts();
        } catch (RmesException e) {
            return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
    }

    @GET()
    @Path("/structures")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllStructures", summary = "Get all structures")
    public Response getAllStructures() {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getAllStructures();
        } catch (RmesException e) {
            return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
    }

    @GET()
    @Path("/composants")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllComponents", summary = "Get all components")
    public Response getAllComponents() {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getAllComponents();
        } catch (RmesException e) {
            return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
    }

    @GET()
    @Path("/composant/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getComponentById", summary = "Get a component")
    public Response getComponentById(@PathParam(Constants.ID) String id) {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getComponent(id);
        } catch (RmesException e) {
            return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
    }

    @GET()
    @Path("/structure/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getStructure", summary = "Get a structure")
    public Response getStructure(@PathParam(Constants.ID) String id) {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getStructure(id);
        } catch (RmesException e) {
            return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
    }

    @GET()
    @Path("/listesCodes")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllCodesLists", summary = "Get all codes lists")
    public Response getAllCodesLists() {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getAllCodesLists();
        } catch (RmesException e) {
            return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
    }

    @GET()
    @Path("/listeCode/{notation}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getCodesList", summary = "Get one codes list")
    public Response getCodesList(@PathParam(Constants.NOTATION) String notation) {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getCodesList(notation);
        } catch (RmesException e) {
            return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
    }

}
