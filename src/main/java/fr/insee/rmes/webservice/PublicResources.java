package fr.insee.rmes.webservice;

import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.persistance.disseminationStatus.DisseminationStatus;
import fr.insee.rmes.persistance.securityManager.RmesSecurityManagerImpl;
import fr.insee.rmes.persistance.securityManager.SecurityManagerContract;
import fr.insee.rmes.persistance.stamps.RmesStampsImpl;
import fr.insee.rmes.persistance.stamps.StampsContract;

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
@Path("/")
public class PublicResources {
	
	@POST
	@Path("/auth")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAuth(String body) {
		SecurityManagerContract sm = new RmesSecurityManagerImpl();
		return Response.status(HttpStatus.SC_OK).entity(sm.getAuth(body)).build();
	}
	
	@GET
	@Path("/stamps")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStamps() {
		StampsContract stampsContract = new RmesStampsImpl();
		return Response.status(HttpStatus.SC_OK).entity(stampsContract.getStamps()).build();
	}
	
	@GET
	@Path("/disseminationStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDisseminationStatus() {
		TreeSet<String> dsList = new TreeSet<String>();
		for (DisseminationStatus ds : DisseminationStatus.values()) {
			try {
				dsList.add(new ObjectMapper().writeValueAsString(ds));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return Response.status(HttpStatus.SC_OK).entity(dsList.toString()).build();
	}
	
	@GET
	@Path("/roles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoles() {
		SecurityManagerContract sm = new RmesSecurityManagerImpl();
		return Response.status(HttpStatus.SC_OK).entity(sm.getRoles()).build();
	}
	
	@GET
	@Path("/agents")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgents() {
		SecurityManagerContract sm = new RmesSecurityManagerImpl();
		return Response.status(HttpStatus.SC_OK).entity(sm.getAgents()).build();
	}
	
	@POST
	@Path("/private/role/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setAddRole(String body) {
		SecurityManagerContract sm = new RmesSecurityManagerImpl();
		sm.setAddRole(body);
	}
	
	@POST
	@Path("/private/role/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setDeleteRole(String body) {
		SecurityManagerContract sm = new RmesSecurityManagerImpl();
		sm.setDeleteRole(body);
	}
}