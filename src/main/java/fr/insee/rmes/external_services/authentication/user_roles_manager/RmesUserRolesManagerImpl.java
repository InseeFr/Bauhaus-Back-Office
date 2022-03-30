package fr.insee.rmes.external_services.authentication.user_roles_manager;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.authentication.LdapConnexion;
import fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel.Application;
import fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel.Group;
import fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel.UserSugoi;
import fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel.UsersSugoi;
import fr.insee.rmes.utils.JSONComparator;

@Service
public class RmesUserRolesManagerImpl implements UserRolesManagerService {
	
	private static final int NB_USERS_EXPECTED = 20000;

	@Autowired
	LdapConnexion ldapConnexion;
	
	@Autowired
	Config config;

	static final Logger  logger = LogManager.getLogger(RmesUserRolesManagerImpl.class);

	private static final String SUGOI_REALM_SEARCH_PATH = "/realms/";
	private static final String SUGOI_APP_SEARCH_PATH = "/applications/";

			
	private Map<String,UserSugoi> mapUsers;
	

	@Override
	public String getRoles() throws RmesException {
		 String searchAppSugoiTarget = config.getSugoiUrl() + SUGOI_REALM_SEARCH_PATH + config.getSugoiRealm() + SUGOI_APP_SEARCH_PATH + config.getSugoiApp() ;
		
		if (mapUsers == null || mapUsers.isEmpty()) {getAgentsSugoi();}
		logger.info("mapUsers size : {} / {} max", mapUsers.size(), NB_USERS_EXPECTED);
		JSONArray roles = new JSONArray();
		try {
			Client client = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(config.getSugoiUser(), config.getSugoiPassword()));	
			String jsonResponse = client.target(searchAppSugoiTarget).request(MediaType.APPLICATION_JSON).get(String.class);

			ObjectMapper mapper = new ObjectMapper();
			Application application = mapper.readValue(jsonResponse, Application.class);
			for (Group g : application.getGroups()) {
				JSONObject jsonGroup = new JSONObject();
				jsonGroup.put(Constants.ID, g.getName());
				jsonGroup.put(Constants.LABEL, g.getDescription());
				JSONArray persons = new JSONArray();
				if (g.getUsers() != null) {
					for (UserSugoi u : g.getUsers()) {
						UserSugoi completeUser = mapUsers.get(u.getUsername().toLowerCase());
						if (completeUser != null ) {
							JSONObject jsonUser = new JSONObject();
							jsonUser.put(Constants.ID, u.getUsername());
							jsonUser.put(Constants.LABEL, completeUser.getCompleteName());
							jsonUser.put(Constants.STAMP, completeUser.getAttributes().getInseeTimbre());
							persons.put(jsonUser);
						}	else logger.warn("Unknown user : {}",u.getUsername());
					}
				}
				jsonGroup.put("persons", persons);
				roles.put(jsonGroup);
			}
			
		} catch (Exception e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Fail to getRoles");
		}
		return roles.toString();
	}

	public String getAgentsSugoi() throws RmesException {
		String searchUserSugoiTarget = config.getSugoiUrl() + SUGOI_REALM_SEARCH_PATH + config.getSugoiRealm() + "/users" ;
		mapUsers = new HashMap<>(NB_USERS_EXPECTED);
		
		TreeSet<JSONObject> agents = new TreeSet<>(new JSONComparator(Constants.LABEL));

		Client client = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(config.getSugoiUser(), config.getSugoiPassword()));	
		String jsonResponse = client.target(searchUserSugoiTarget)
									.queryParam("size", NB_USERS_EXPECTED)
									.request(MediaType.APPLICATION_JSON)
									.get(String.class);
		
		ObjectMapper mapper = new ObjectMapper();
		UsersSugoi users;
		try {
			users = mapper.readValue(jsonResponse, UsersSugoi.class);
			
			for (UserSugoi u : users.getResults()) {
				JSONObject jsonUser = new JSONObject();
				jsonUser.put(Constants.ID, u.getUsername());
				jsonUser.put(Constants.LABEL, u.getCompleteName());
				if (u.getAttributes() != null) {
					jsonUser.put(Constants.STAMP, u.getAttributes().getInseeTimbre());
				}
				agents.add(jsonUser);
				mapUsers.put(u.getUsername().toLowerCase(), u);
			}
		} catch (JsonProcessingException e) {
			logger.error("Get agents via Sugoi failed : {}", e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Get agents via Sugoi failed");
		}
		return agents.toString();
	}

	@Override
	public void setAddRole(String role, String user) throws  RmesException{
		String url = MessageFormat.format(getUserPath(), user, role);
		Sugoi.put(url);
	}

	@Override
	public void setDeleteRole(String role, String user) throws  RmesException {
		String url = MessageFormat.format(getUserPath(), user, role);
			Sugoi.delete(url);
	}
	
	private String getUserPath() {
		return config.getSugoiUrl() + "/v2"+ SUGOI_REALM_SEARCH_PATH + config.getSugoiRealm() + SUGOI_APP_SEARCH_PATH+ config.getSugoiApp()+"/groups/{1}/members/{0}";
	}

	
	@Override
	public String checkSugoiConnexion() throws RmesException {
		String jsonResponse ="";
		try {
			Client client = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(config.getSugoiUser(), config.getSugoiPassword()));

			jsonResponse = client.target(config.getSugoiUrl() + "whoami")
					.request(MediaType.APPLICATION_JSON).get(String.class);
		} catch (Exception e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Fail to target SUGOI");
		}
		return StringUtils.isEmpty(jsonResponse)? "KO" :  "OK";
	}

}
