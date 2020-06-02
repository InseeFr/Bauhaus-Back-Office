package fr.insee.rmes.external_services.authentication.user_roles_manager;

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

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.utils.JSONComparator;

@Service
public class RmesUserRolesManagerImpl implements UserRolesManagerService {

	static final Logger logger = LogManager.getLogger(RmesUserRolesManagerImpl.class);

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
	@SuppressWarnings("unchecked")
	public String getRoles() throws RmesException {
		JSONArray roles = new JSONArray();
		try {
			Client client = ClientBuilder.newClient();

			String xmlResponse = client.target(Config.IGESA_URL + IGESA_APP_SEARCH_PATH + Config.IGESA_APP_ID)
					.request(MediaType.APPLICATION_XML).get(String.class);

			Document doc = new SAXBuilder().build(new StringReader(xmlResponse));
			List<Element> l = (XPath.selectNodes(doc, ROLES_XPATH));
			for (Element e : l) {
				JSONObject jsonO = new JSONObject();
				jsonO.put(Constants.ID, XPath.newInstance(ROLE_ID_XPATH).valueOf(e));
				jsonO.put(Constants.LABEL, XPath.newInstance(ROLE_LABEL_XPATH).valueOf(e));
				List<Element> p = (XPath.selectNodes(e, ROLE_PERSONS_XPATH));
				JSONArray persons = new JSONArray();
				for (Element person : p) {
					JSONObject jsonOO = new JSONObject();
					jsonOO.put(Constants.ID, XPath.newInstance(ROLE_PERSON_IDEP_XPATH).valueOf(person));
					jsonOO.put(Constants.LABEL, XPath.newInstance(ROLE_ID_XPATH).valueOf(person));
					jsonOO.put("stamp", XPath.newInstance(ROLE_PERSON_STAMP_XPATH).valueOf(person));
					persons.put(jsonOO);
				}
				jsonO.put("persons", persons);
				roles.put(jsonO);
			}
		} catch (Exception e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Fail to getRoles");
		}
		return roles.toString();
	}

	@Override
	public String getAgents() throws RmesException {
		TreeSet<JSONObject> agents = new TreeSet<>(new JSONComparator(Constants.LABEL));
		logger.info("Connection to LDAP : {0}", Config.LDAP_URL);
		try {
			// Connexion à la racine de l'annuaire
			Hashtable<String, String> environment = new Hashtable<>();
			environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, Config.LDAP_URL);
			environment.put(Context.SECURITY_AUTHENTICATION, "none");
			DirContext context;

			context = new InitialDirContext(environment);

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
			logger.error("Get agents failed : {0}", e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Get agents failed");
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

}
