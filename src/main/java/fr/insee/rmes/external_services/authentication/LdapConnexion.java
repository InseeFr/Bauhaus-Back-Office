package fr.insee.rmes.external_services.authentication;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

@Service
public class LdapConnexion {
	
	  private LdapConnexion() {
		    throw new IllegalStateException("Utility class");
	  }
	
	static final Logger logger = LogManager.getLogger(LdapConnexion.class);

	public static DirContext getLdapContext() throws NamingException, RmesException {
		if(Config.LDAP_URL != null && !Config.LDAP_URL.isEmpty()) {
			logger.info("Connection to LDAP : {}", Config.LDAP_URL);
			// Connexion à la racine de l'annuaire
			Hashtable<String, String> environment = new Hashtable<>();
			environment.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, Config.LDAP_URL);
			environment.put(Context.SECURITY_AUTHENTICATION, "none");
			return new InitialDirContext(environment);
		}else throw new RmesException(500, "LDAP not found", "Config file is null or empty");
	}


}
