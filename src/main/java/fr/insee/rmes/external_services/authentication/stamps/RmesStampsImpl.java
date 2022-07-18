package fr.insee.rmes.external_services.authentication.stamps;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.authentication.LdapConnexion;
import fr.insee.rmes.external_services.authentication.stamps.apiRHModel.Content;
import fr.insee.rmes.external_services.authentication.stamps.apiRHModel.Stamp;

@Service
public class RmesStampsImpl implements StampsService {
	
	static final Logger logger = LogManager.getLogger(RmesStampsImpl.class);

	private static final String APIRH_SEARCH_PATH = "/unites?size=2000&page="  ; // "/unites?page=";
	private static final String APIRH_SEARCH_STAMP = Config.getApiRhUrl() + APIRH_SEARCH_PATH;
	
	@Autowired
	Config config;
	
	@Autowired
	StampsRestrictionsService stampsRestrictionService; 
	
	@Autowired
	LdapConnexion ldapConnexion;
	
	private static InputStream executeGet(String httpMethod)  throws RmesException, UnsupportedOperationException, IOException{
		HttpResponse response ;
		HttpGet http = new HttpGet(httpMethod);
		try {
			http.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
			http.setHeader("Content-type", MediaType.APPLICATION_JSON_VALUE);
			HttpClient httpclient = HttpClientBuilder.create().build();
			response = httpclient.execute(http);
			int status = response.getStatusLine().getStatusCode();
			if (status <200 || status>299) {
				throw new RmesException(status, "Error with Api RH", "Api RH error");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),e.getClass().getName());
		}
			return response.getEntity().getContent();
	}	
	
	public String getStampsApiRH() throws RmesException {
		TreeSet<String> listStamp = new TreeSet<>();
		for (int i=0;i<=2;i++ ) {		
			String jsonResponse;
			try {
				jsonResponse = new String(executeGet(APIRH_SEARCH_STAMP+i).readAllBytes(), StandardCharsets.UTF_8);
			} catch (UnsupportedOperationException | IOException | RmesException e1) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e1.getMessage(), "Get agents via apiRH failed");
			}
			ObjectMapper mapper = new ObjectMapper();
			Stamp stamp;
			try {
				stamp = mapper.readValue(jsonResponse, Stamp.class);
				for (Content u : stamp.getContent()) {
					JSONObject jsonStamp = new JSONObject();
					if(u.getTimbre() != null) {
					jsonStamp.put("timbre", u.getTimbre());
					String input = (String) jsonStamp.get("timbre");				
					listStamp.add(JSONObject.valueToString(input));		
						}
					}
				
			    } catch (JsonProcessingException e) {
				logger.error("Get agents via apiRH failed : {}", e.getMessage());
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Get agents via apiRH failed");
			}
		}
		// Add SSM Stamps
		listStamp.add("\"SSM-SSP\"");
		listStamp.add("\"SSM-DARES\"");
		listStamp.add("\"SSM-DEPP\"");
		listStamp.add("\"SSM-DESL\"");
		listStamp.add("\"SSM-DREES\"");
		listStamp.add("\"SSM-SDES\"");
		listStamp.add("\"SSM-SDSE\"");
		listStamp.add("\"SSM-MEOS\"");
		listStamp.add("\"SSM-OED\"");
		listStamp.add("\"SSM-DSEE\"");
		listStamp.add("\"SSM-SSMSI\"");
		listStamp.add("\"SSM-GF3C\"");
		listStamp.add("\"SSM-DSED\"");
		listStamp.add("\"SSM-DESSI\"");
		listStamp.add("\"SSM-SIES\"");
		listStamp.add("\"SSM-DEPS\"");
		
		return listStamp.toString();
	}
	
	@Override
	public String getStamps() throws RmesException {
		TreeSet<String> stamps = new TreeSet<>();
		try {
			if(config.getLdapUrl() != null && !config.getLdapUrl().isEmpty()) {
				// Connexion à la racine de l'annuaire
				DirContext context = ldapConnexion.getLdapContext();

				// Spécification des critères pour la recherche des unités
				SearchControls controls = new SearchControls();
				controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				controls.setReturningAttributes(new String[] { "ou", "description" });
				String filter = "(objectClass=inseeUnite)";

				// Exécution de la recherche et parcours des résultats
				NamingEnumeration<SearchResult> results = context.search(
						"ou=Unités,o=insee,c=fr", filter, controls);
				while (results.hasMore()) {
					SearchResult entree = results.next();
					String stamp = entree.getAttributes().get("ou").get()
							.toString();
					if(!stamp.equals("AUTRE")) {
						stamps.add("\"" + stamp + "\"");
					}
				}
				context.close();
			}

			// Add SSM Stamps
			stamps.add("\"SSM-SSP\"");
			stamps.add("\"SSM-DARES\"");
			stamps.add("\"SSM-DEPP\"");
			stamps.add("\"SSM-DESL\"");
			stamps.add("\"SSM-DREES\"");
			stamps.add("\"SSM-SDES\"");
			stamps.add("\"SSM-SDSE\"");
			stamps.add("\"SSM-MEOS\"");
			stamps.add("\"SSM-OED\"");
			stamps.add("\"SSM-DSEE\"");
			stamps.add("\"SSM-SSMSI\"");
			stamps.add("\"SSM-GF3C\"");
			stamps.add("\"SSM-DSED\"");
			stamps.add("\"SSM-DESSI\"");
			stamps.add("\"SSM-SIES\"");
			stamps.add("\"SSM-DEPS\"");
			
			logger.info("Get stamps succeed");
		} catch (NamingException e) {
			logger.error("Get stamps failed");
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Get stamps failed");		
		}		
		return stamps.toString();
	}
	
	@Override
	public String getStamp() throws RmesException {
		JSONObject jsonStamp = new JSONObject();
		jsonStamp.put("stamp",stampsRestrictionService.getUser().getStamp());
		return jsonStamp.toString();
	}

}
