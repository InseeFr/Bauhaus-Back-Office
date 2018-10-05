package fr.insee.rmes.persistance.user_roles_manager;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.user_roles_manager.rmes.Igesa;
import fr.insee.rmes.persistance.user_roles_manager.rmes.JSONComparator;

@Service
public class RmesUserRolesManagerImpl implements UserRolesManagerService {

	final static Logger logger = LogManager.getLogger(RmesUserRolesManagerImpl.class);

	private static final String IGESA_APP_SEARCH_PATH = "/recherche/application/";

	private static final String IGESA_ADD_USER_PATH_FMT = Config.IGESA_URL + "/gestion/ajout/personne/application/"
			+ Config.IGESA_APP_ID + "/groupe/{1}/utilisateur/{0}";
	private static final String IGESA_DELETE_USER_PATH_FMT = Config.IGESA_URL
			+ "/gestion/suppression/personne/application/" + Config.IGESA_APP_ID + "/groupe/{1}/utilisateur/{0}";

	private static final String ROLES_XPATH = "/applications/application/groupes/groupe";
	private static final String ROLE_ID_XPATH = "cn";
	private static final String ROLE_LABEL_XPATH = "description";
	private static final String ROLE_PERSONS_XPATH = "personnes/personne";
	private static final String ROLE_PERSON_STAMP_XPATH = "ou";
	private static final String ROLE_PERSON_IDEP_XPATH = "uid";
	private static final String ROLE_PERSON_LABEL_XPATH = "cn";
	
	public String getAuth(String body) {
		if (body.equals(Config.PASSWORD_GESTIONNAIRE)) return "GESTIONNAIRE";
		if (body.equals(Config.PASSWORD_PRODUCTEUR)) return "PRODUCTEUR";
		return "NONE";
	}

	@SuppressWarnings("unchecked")
	public String getRoles() throws RmesException {
		JSONArray roles = new JSONArray();
		try {
			Client client = ClientBuilder.newClient();

			String xmlResponse = client.target(Config.IGESA_URL + IGESA_APP_SEARCH_PATH + Config.IGESA_APP_ID)
					.request(MediaType.APPLICATION_XML).get(String.class);

			Document doc = new SAXBuilder().build(new StringReader(xmlResponse));
			List<Element> l = (List<Element>) (XPath.selectNodes(doc, ROLES_XPATH));
			for (Element e : l) {
				JSONObject jsonO = new JSONObject();
				jsonO.put("id", XPath.newInstance(ROLE_ID_XPATH).valueOf(e));
				jsonO.put("label", XPath.newInstance(ROLE_LABEL_XPATH).valueOf(e));
				List<Element> p = (List<Element>) (XPath.selectNodes(e, ROLE_PERSONS_XPATH));
				JSONArray persons = new JSONArray();
				for (Element person : p) {
					JSONObject jsonOO = new JSONObject();
					jsonOO.put("id", XPath.newInstance(ROLE_PERSON_IDEP_XPATH).valueOf(person));
					jsonOO.put("label", XPath.newInstance(ROLE_PERSON_LABEL_XPATH).valueOf(person));
					jsonOO.put("stamp", XPath.newInstance(ROLE_PERSON_STAMP_XPATH).valueOf(person));
					persons.put(jsonOO);
				}
				jsonO.put("persons", persons);
				roles.put(jsonO);
			}
		} catch (Exception e) {
			throw new RmesException(500, e.getMessage(), "Fail to getRoles");
		}
		return roles.toString();
	}

	public String getAgents() throws RmesException {
		TreeSet<JSONObject> agents = new TreeSet<JSONObject>(new JSONComparator("label"));
		logger.info("Connection to LDAP : " + Config.LDAP_URL);
		try {
			// Connexion à la racine de l'annuaire
			Hashtable<String, String> environment = new Hashtable<String, String>();
			environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, Config.LDAP_URL);
			environment.put(Context.SECURITY_AUTHENTICATION, "none");
			DirContext context;

			context = new InitialDirContext(environment);

			// Spécification des critères pour la recherche des unités
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(new String[] { "cn", "uid" });
			String filter = "(&(objectClass=inseePerson)(!(inseeFonction=Enqueteur de l'INSEE*)))";

			// Exécution de la recherche et parcours des résultats
			NamingEnumeration<SearchResult> results = context.search("o=insee,c=fr", filter, controls);
			while (results.hasMore()) {
				SearchResult entree = results.next();
				JSONObject jsonO = new JSONObject();
				jsonO.put("label", entree.getAttributes().get("cn").get().toString());
				jsonO.put("id", entree.getAttributes().get("uid").get().toString());
				agents.add(jsonO);
			}
			context.close();
			logger.info("Get agents succeed");
		} catch (NamingException e) {
			logger.error("Get agents failed : " + e.getMessage());
			throw new RmesException(500, e.getMessage(), "Get agents failed");
		}
		return agents.toString();
	}

	public void setAddRole(String body) {
		JSONArray agents = new JSONArray(body);
		agents.forEach(item -> {
			JSONObject agent = (JSONObject) item;
			agent.getJSONArray("roles").forEach(r -> {
				String URL = MessageFormat.format(IGESA_ADD_USER_PATH_FMT, agent.getString("id"), (String) r);
				Igesa.post(URL);
			});
		});
	}

	public void setDeleteRole(String body) {
		JSONArray agents = new JSONArray(body);
		agents.forEach(item -> {
			JSONObject agent = (JSONObject) item;
			String URL = MessageFormat.format(IGESA_DELETE_USER_PATH_FMT, agent.getString("id"), agent.getString("role"));
			Igesa.post(URL);
		});
	}

}
