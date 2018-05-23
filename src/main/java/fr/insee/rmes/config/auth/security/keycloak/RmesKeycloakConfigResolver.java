package fr.insee.rmes.config.auth.security.keycloak;

import java.io.File;
import java.io.InputStream;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;

public class RmesKeycloakConfigResolver implements KeycloakConfigResolver {

	@Override
	public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak-back/keycloak-local.json");
		
		String url = String.format("%s/webapps/%s", System.getProperty("catalina.base"), "keycloak-back/bauhaus-dev.properties");
		File f = new File(url);
		if(f.exists() && !f.isDirectory()) { 
			is = getClass().getClassLoader().getResourceAsStream(url);
		}
		
		url = String.format("%s/webapps/%s", System.getProperty("catalina.base"), "keycloak-back/bauhaus-qf.properties");
		File f1 = new File(url);
		if(f1.exists() && !f1.isDirectory()) { 
			is = getClass().getClassLoader().getResourceAsStream(url);
		}
		
		url = String.format("%s/webapps/%s", System.getProperty("catalina.base"), "keycloak-back/bauhaus-pre-prod.properties");
		File f2 = new File(url);
		if(f2.exists() && !f2.isDirectory()) { 
			is = getClass().getClassLoader().getResourceAsStream(url);
		}
		
		url = String.format("%s/webapps/%s", System.getProperty("catalina.base"), "keycloak-back/bauhaus-prod.properties");
		File f3 = new File(url);
		if(f3.exists() && !f3.isDirectory()) { 
			is = getClass().getClassLoader().getResourceAsStream(url);
		}
		
		return KeycloakDeploymentBuilder.build(is);
	}

}
