package fr.insee.rmes.config.auth.security.keycloak;

import java.io.File;
import java.io.InputStream;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;

import fr.insee.rmes.utils.FileUtils;

public class RmesKeycloakConfigResolver implements KeycloakConfigResolver {
	
	@Override
	public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak-back-local.json");
		
		String url = String.format("%s/webapps/%s", System.getProperty("catalina.base"), "keycloak-back-dev.json");
		File f = new File(url);
		if(f.exists() && !f.isDirectory()) { 
			is = FileUtils.fileToIS(f);
		}
		
		url = String.format("%s/webapps/%s", System.getProperty("catalina.base"), "keycloak-back-qf.json");
		File f1 = new File(url);
		if(f1.exists() && !f1.isDirectory()) {
			is = FileUtils.fileToIS(f1);
		}
		
		url = String.format("%s/webapps/%s", System.getProperty("catalina.base"), "keycloak-back-pre-prod.json");
		File f2 = new File(url);
		if(f2.exists() && !f2.isDirectory()) { 
			is = FileUtils.fileToIS(f2);
		}
		
		url = String.format("%s/webapps/%s", System.getProperty("catalina.base"), "keycloak-back-prod.json");
		File f3 = new File(url);
		if(f3.exists() && !f3.isDirectory()) { 
			is = FileUtils.fileToIS(f3);
		}
		
		return KeycloakDeploymentBuilder.build(is);
	}

}
