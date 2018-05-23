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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.manager.SecurityManagerService;
import fr.insee.rmes.config.roles.UserRolesManagerService;
import fr.insee.rmes.persistance.disseminationStatus.DisseminationStatus;
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

	final static Logger logger = LogManager.getLogger(PublicResources.class);

	@Autowired
	SecurityManagerService securityManagerService;

	@Autowired
	UserRolesManagerService userRolesManagerService;

	@GET
	@Path("/init")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProperties() {
		JSONObject props = new JSONObject();
		try {
			props.put("appHost", Config.APP_HOST);
			props.put("defaultContributor", Config.DEFAULT_CONTRIBUTOR);
			props.put("defaultMailSender", Config.DEFAULT_MAIL_SENDER);
			props.put("maxLengthScopeNote", Config.MAX_LENGTH_SCOPE_NOTE);
			props.put("lg1", Config.LG1);
			props.put("lg2", Config.LG2);
			props.put("authType", securityManagerService.getAuthType());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		return Response.status(HttpStatus.SC_OK).entity(props.toString()).build();
	}

	@POST
	@Path("/auth")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAuth(String body) {
		return Response.status(HttpStatus.SC_OK).entity(securityManagerService.postAuth(body)).build();
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
		return Response.status(HttpStatus.SC_OK).entity(userRolesManagerService.getRoles()).build();
	}

	@GET
	@Path("/agents")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgents() {
		return Response.status(HttpStatus.SC_OK).entity(userRolesManagerService.getAgents()).build();
	}

	@POST
	@Path("/private/role/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setAddRole(String body) {
		userRolesManagerService.setAddRole(body);
	}

	@POST
	@Path("/private/role/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setDeleteRole(String body) {
		userRolesManagerService.setDeleteRole(body);
	}
}