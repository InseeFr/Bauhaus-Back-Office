package fr.insee.rmes.service.authentication.stamps;

import java.util.Hashtable;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

@Service
public class RmesStampsImpl implements StampsService {
	
	final static Logger logger = LogManager.getLogger(RmesStampsImpl.class);

	@Override
	public String getStamps() throws RmesException {
		TreeSet<String> stamps = new TreeSet<String>();
		logger.info("Connection to LDAP : " + Config.LDAP_URL);
		try {
			// Connexion à la racine de l'annuaire
			Hashtable<String, String> environment = new Hashtable<String, String>();
			environment.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, Config.LDAP_URL);
			environment.put(Context.SECURITY_AUTHENTICATION, "none");
			DirContext context;

			context = new InitialDirContext(environment);

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
			
			context.close();
			logger.info("Get stamps succeed");
		} catch (NamingException e) {
			logger.error("Get stamps failed");
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Get stamps failed");		
		}		
		return stamps.toString();
	}

}
