package fr.insee.rmes.external_services.authentication.user_roles_manager;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
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

	static final Logger logger = LogManager.getLogger(RmesUserRolesManagerImpl.class);

	private static final String IGESA_APP_SEARCH_PATH = "/recherche/application/";
	private static final String SUGOI_REALM_SEARCH_PATH = "/realms/";
	private static final String SUGOI_APP_SEARCH_PATH = "/applications/";
	private static final String SUGOI_SEARCH_APP = Config.SUGOI_URL + SUGOI_REALM_SEARCH_PATH + Config.SUGOI_REALM + SUGOI_APP_SEARCH_PATH + Config.SUGOI_APP ;
	private static final String SUGOI_SEARCH_USERS = Config.SUGOI_URL + SUGOI_REALM_SEARCH_PATH + Config.SUGOI_REALM + "/users" ;


	private static final String IGESA_ADD_USER_PATH_FMT = Config.IGESA_URL + "/gestion/ajout/personne/application/"
			+ Config.IGESA_APP_ID + "/groupe/{1}/utilisateur/{0}";
	private static final String IGESA_DELETE_USER_PATH_FMT = Config.IGESA_URL
			+ "/gestion/suppression/personne/application/" + Config.IGESA_APP_ID + "/groupe/{1}/utilisateur/{0}";

	private static final String ROLE_ID_XPATH = "cn";
	private static final String ROLE_PERSON_IDEP_XPATH = "uid";
	
	private Map<String,UserSugoi> mapUsers;
	
	@Override
	public String getAuth(String body) {
		if (body.equals(Config.PASSWORD_GESTIONNAIRE)) {
			return "GESTIONNAIRE";
		}
		if (body.equals(Config.PASSWORD_PRODUCTEUR)) {
			return "PRODUCTEUR";
		}
		return "NONE";
	}

	@Override
	public String getRoles() throws RmesException {
		if (mapUsers == null || mapUsers.isEmpty()) {getAgentsSugoi();}
		logger.info("mapUsers size : {} / {} max", mapUsers.size(), NB_USERS_EXPECTED);
		JSONArray roles = new JSONArray();
		try {
			Client client = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(Config.SUGOI_USER, Config.SUGOI_PASSWORD));	
			String jsonResponse = client.target(SUGOI_SEARCH_APP).request(MediaType.APPLICATION_JSON).get(String.class);

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

	@Override
	public String getAgents() throws RmesException {
		TreeSet<JSONObject> agents = new TreeSet<>(new JSONComparator(Constants.LABEL));
		logger.info("Connection to LDAP : {}", Config.LDAP_URL);
		try {
			// Connexion à la racine de l'annuaire
			DirContext context = ldapConnexion.getLdapContext();

			// Spécification des critères pour la recherche des unités
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(new String[] { ROLE_ID_XPATH,ROLE_PERSON_IDEP_XPATH });
			String filter = "(&(objectClass=inseePerson)(!(inseeFonction=Enqueteur de l'INSEE*)))";

			// Exécution de la recherche et parcours des résultats
			NamingEnumeration<SearchResult> results = context.search("o=insee,c=fr", filter, controls);
			while (results.hasMore()) {
				SearchResult entree = results.next();
				JSONObject jsonO = new JSONObject();
				jsonO.put(Constants.LABEL, entree.getAttributes().get(ROLE_ID_XPATH).get().toString());
				jsonO.put(Constants.ID, entree.getAttributes().get(ROLE_PERSON_IDEP_XPATH).get().toString());
				agents.add(jsonO);
			}
			context.close();
			logger.info("Get agents succeed");
		} catch (NamingException e) {
			logger.error("Get agents failed : {}", e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Get agents failed");
		}
		return agents.toString();
	}
	
	
	public String getAgentsSugoi() throws RmesException {
		mapUsers = new HashMap<>(NB_USERS_EXPECTED);
		TreeSet<JSONObject> agents = new TreeSet<>(new JSONComparator(Constants.LABEL));

		Client client = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(Config.SUGOI_USER, Config.SUGOI_PASSWORD));	
		String jsonResponse = client.target(SUGOI_SEARCH_USERS)
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
	public void setAddRole(String role, String user) {
		String url = MessageFormat.format(IGESA_ADD_USER_PATH_FMT, user, role);
		Igesa.post(url);
	}

	@Override
	public void setDeleteRole(String role, String user) {
		String url = MessageFormat.format(IGESA_DELETE_USER_PATH_FMT, user, role);
		Igesa.post(url);
	}

	@Override
	public String checkLdapConnexion() throws RmesException {
		//IGESA
		String xmlResponse ="";
		try {
			Client client = ClientBuilder.newClient();

			xmlResponse = client.target(Config.IGESA_URL + IGESA_APP_SEARCH_PATH + Config.IGESA_APP_ID)
					.request(MediaType.APPLICATION_XML).get(String.class);
		} catch (Exception e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Fail to target IGESA");
		}
		return StringUtils.isEmpty(xmlResponse)? "KO" :  "OK";
	}
	
	@Override
	public String checkSugoiConnexion() throws RmesException {
		String jsonResponse ="";
		try {
			Client client = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(Config.SUGOI_USER, Config.SUGOI_PASSWORD));

			jsonResponse = client.target(Config.SUGOI_URL + "whoami")
					.request(MediaType.APPLICATION_JSON).get(String.class);
		} catch (Exception e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Fail to target SUGOI");
		}
		return StringUtils.isEmpty(jsonResponse)? "KO" :  "OK";
	}

}
