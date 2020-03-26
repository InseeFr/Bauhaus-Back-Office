package fr.insee.rmes.config.auth.security.keycloak;

import java.io.File;
import java.io.InputStream;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;

import fr.insee.rmes.utils.FileUtils;

public class RmesKeycloakConfigResolver implements KeycloakConfigResolver {
	
	private static final String CATALINA_BASE = "catalina.base";
	private static final String S_WEBAPPS_S = "%s/webapps/%s";

	@Override
	public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak-back-local.json");
		
		String url = String.format(S_WEBAPPS_S, System.getProperty(CATALINA_BASE), "keycloak-back-dev.json");
		is = updateInputStreamIfFileExists(is, url);
		
		url = String.format(S_WEBAPPS_S, System.getProperty(CATALINA_BASE), "keycloak-back-qf.json");
		is = updateInputStreamIfFileExists(is, url);
		
		url = String.format(S_WEBAPPS_S, System.getProperty(CATALINA_BASE), "keycloak-back-pre-prod.json");
		is = updateInputStreamIfFileExists(is, url);
		
		url = String.format(S_WEBAPPS_S, System.getProperty(CATALINA_BASE), "keycloak-back-prod.json");
		is = updateInputStreamIfFileExists(is, url);
		
		return KeycloakDeploymentBuilder.build(is);
	}

	private InputStream updateInputStreamIfFileExists(InputStream is, String url) {
		File f = new File(url);
		if(f.exists() && !f.isDirectory()) { 
			is = FileUtils.fileToIS(f);
		}
		return is;
	}

}
