package fr.insee.rmes.external_services.authentication.stamps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.authentication.LdapConnexion;
import fr.insee.rmes.external_services.authentication.stamps.apiRHModel.Content;
import fr.insee.rmes.external_services.authentication.stamps.apiRHModel.Stamp;
import fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel.Application;
import fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel.UserSugoi;
import fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel.UsersSugoi;
import fr.insee.rmes.utils.JSONComparator;

@Service
public class RmesStampsImpl implements StampsService {
	
	static final Logger logger = LogManager.getLogger(RmesStampsImpl.class);

	private static final String APIRH_SEARCH_PATH = "/unites?size=2000&page="  ; // "/unites?page=";
	private static final String APIRH_SEARCH_STAMP = Config.APIRH_URL + APIRH_SEARCH_PATH;
	
	@Autowired
	StampsRestrictionsService stampsRestrictionService; 
	
	@Autowired
	LdapConnexion ldapConnexion;
	
	
	public String getStampsApiRH() throws RmesException {
		
		TreeSet<String> listStamp = new TreeSet<>();

		for (int i=0;i<=2;i++ ) {
			
		Client client = ClientBuilder.newClient();	
		String jsonResponse = client.target(APIRH_SEARCH_STAMP+i).request(MediaType.APPLICATION_JSON).get(String.class);
		ObjectMapper mapper = new ObjectMapper();
		Stamp stamp;
		try {
			stamp = mapper.readValue(jsonResponse, Stamp.class);
			
				
			for (Content u : stamp.getContent()) {
				
				JSONObject jsonStamp = new JSONObject();
				if(u.getTimbre() != null) {
				jsonStamp.put(Constants.STAMP, u.getTimbre());
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
	public String getStamp() throws RmesException {
		JSONObject jsonStamp = new JSONObject();
		jsonStamp.put("stamp",stampsRestrictionService.getUser().getStamp());
		return jsonStamp.toString();
	}
	

}
