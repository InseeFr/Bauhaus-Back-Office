package fr.insee.rmes.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource(value = {
        "classpath:gncs-core.properties",
        "classpath:gncs-dev.properties",
        "file:${catalina.base}/webapps/gncs-dev.properties",
        "file:${catalina.base}/webapps/gncs-qf.properties",
        "file:${catalina.base}/webapps/production.properties",
}, ignoreResourceNotFound = true)
public class ApplicationContext {

	@Autowired
    Environment env;
	
	@PostConstruct
	public void setUp() {
		Config.LG1 = env.getProperty("fr.insee.rmes.gncs.lg1");
		Config.LG2 = env.getProperty("fr.insee.rmes.gncs.lg2");
		
		Config.PASSWORD = env.getProperty("fr.insee.rmes.gncs.password");
		
		Config.CONCEPTS_GRAPH = env.getProperty("fr.insee.rmes.gncs.concepts.graph");
		Config.CONCEPTS_SCHEME = env.getProperty("fr.insee.rmes.gncs.concepts.scheme");
		Config.CONCEPTS_BASE_URI = env.getProperty("fr.insee.rmes.gncs.concepts.baseURI");
		Config.COLLECTIONS_BASE_URI = env.getProperty("fr.insee.rmes.gncs.collections.baseURI");
		
		Config.SESAME_SERVER_GESTION = env.getProperty("fr.insee.rmes.gncs.sesame.gestion.sesameServer");
		Config.REPOSITORY_ID_GESTION = env.getProperty("fr.insee.rmes.gncs.sesame.gestion.repository");
		Config.BASE_URI_GESTION = env.getProperty("fr.insee.rmes.gncs.sesame.gestion.baseURI");
		
		Config.SESAME_SERVER_PUBLICATION = env.getProperty("fr.insee.rmes.gncs.sesame.publication.sesameServer");
		Config.REPOSITORY_ID_PUBLICATION = env.getProperty("fr.insee.rmes.gncs.sesame.publication.repository");
		Config.BASE_URI_PUBLICATION = env.getProperty("fr.insee.rmes.gncs.sesame.publication.baseURI");
		
		Config.SPOC_SERVICE_URL = env.getProperty("fr.insee.rmes.gncs.spoc.url");
		Config.SPOC_USER = env.getProperty("fr.insee.rmes.gncs.spoc.user");
		Config.SPOC_PASSWORD = env.getProperty("fr.insee.rmes.gncs.spoc.password");
		
		Config.BROKER_URL = env.getProperty("fr.insee.rmes.gncs.broker.url");
		Config.BROKER_USER = env.getProperty("fr.insee.rmes.gncs.broker.user");
		Config.BROKER_PASSWORD = env.getProperty("fr.insee.rmes.gncs.broker.password");
		
		Config.LDAP_URL = env.getProperty("fr.insee.rmes.gncs.ldap.url");
		
		Config.IGESA_URL = env.getProperty("fr.insee.rmes.gncs.igesa.url");
		Config.IGESA_APP_ID = env.getProperty("fr.insee.rmes.gncs.igesa.id");
		Config.IGESA_USER = env.getProperty("fr.insee.rmes.gncs.igesa.user");
		Config.IGESA_PASSWORD = env.getProperty("fr.insee.rmes.gncs.igesa.password");
		
	}

}
