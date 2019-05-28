package fr.insee.rmes.config;

import org.springframework.core.env.Environment;
public class Config {
	

	public static String APP_HOST = "";
	
	public static String ENV = "";
	
	public static Boolean REQUIRES_SSL = false;
	
	public static String DEFAULT_CONTRIBUTOR = "";
	public static String DEFAULT_MAIL_SENDER = "";
	public static String MAX_LENGTH_SCOPE_NOTE = "";
	
	public static String LG1 = "";
	public static String LG2 = "";
	
	public static String PASSWORD_GESTIONNAIRE = "";
	public static String PASSWORD_PRODUCTEUR = "";
	
	public static String CONCEPTS_GRAPH = "";
	public static String CONCEPTS_SCHEME = "";
	public static String CONCEPTS_BASE_URI = "";
	public static String COLLECTIONS_BASE_URI = "";
	
	public static String OPERATIONS_GRAPH = "";
	public static String OPERATIONS_BASE_URI = "";
	public static String SERIES_BASE_URI = "";
	public static String FAMILIES_BASE_URI = "";
	public static String DOCUMENTATIONS_BASE_URI = "";
	public static String DOCUMENTATIONS_BASE_GRAPH = "";
	public static String DOCUMENTS_BASE_URI = "";
	public static String LINKS_BASE_URI = "";
	public static String DOCUMENTS_GRAPH = "";
	public static String DOCUMENTS_STORAGE = "";
	
	public static String PRODUCTS_GRAPH = "";
	public static String INDICATORS_BASE_URI = "";

	public static String SESAME_SERVER_GESTION = "";
	public static String REPOSITORY_ID_GESTION = "";
	public static String BASE_URI_GESTION = "";
	
	public static String SESAME_SERVER_PUBLICATION = "";
	public static String REPOSITORY_ID_PUBLICATION = "";
	public static String BASE_URI_PUBLICATION = "";
	
	public static String BASE_URI_METADATA_API = "";
	
	public static String SPOC_SERVICE_URL = "";
	public static String SPOC_USER = "";
	public static String SPOC_PASSWORD = "";
	
	public static String BROKER_URL = "";
	public static String BROKER_USER = "";
	public static String BROKER_PASSWORD = "";
	
	public static String LDAP_URL = "";
	
	public static String IGESA_URL = "";
	public static String IGESA_APP_ID = "";
	public static String IGESA_USER = "";
	public static String IGESA_PASSWORD = "";

	public static String SWAGGER_HOST = "";
	public static String SWAGGER_BASEPATH = "";
	public static String SWAGGER_URL = "";

	private Config() {
		throw new IllegalStateException("Utility class");
	}
	
	public static void setConfig(Environment env) {
		Config.APP_HOST = env.getProperty("fr.insee.rmes.bauhaus.concepts.appHost");
		
		Config.ENV = env.getProperty("fr.insee.rmes.bauhaus.env");

		Config.REQUIRES_SSL = Boolean.valueOf(env.getProperty("fr.insee.rmes.bauhaus.force.ssl"));

		Config.DEFAULT_CONTRIBUTOR = env.getProperty("fr.insee.rmes.bauhaus.concepts.defaultContributor");
		Config.DEFAULT_MAIL_SENDER = env.getProperty("fr.insee.rmes.bauhaus.concepts.defaultMailSender");
		Config.MAX_LENGTH_SCOPE_NOTE = env.getProperty("fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote");

		Config.LG1 = env.getProperty("fr.insee.rmes.bauhaus.lg1");
		Config.LG2 = env.getProperty("fr.insee.rmes.bauhaus.lg2");

		Config.PASSWORD_GESTIONNAIRE = env.getProperty("fr.insee.rmes.bauhaus.gestionnaire.password");
		Config.PASSWORD_PRODUCTEUR = env.getProperty("fr.insee.rmes.bauhaus.producteur.password");

		Config.CONCEPTS_GRAPH = env.getProperty("fr.insee.rmes.bauhaus.concepts.graph");
		Config.CONCEPTS_SCHEME = env.getProperty("fr.insee.rmes.bauhaus.concepts.scheme");
		Config.CONCEPTS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.concepts.baseURI");
		Config.COLLECTIONS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.collections.baseURI");
		
		Config.OPERATIONS_GRAPH = env.getProperty("fr.insee.rmes.bauhaus.operations.graph");
		Config.OPERATIONS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.operations.baseURI");
		Config.SERIES_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.series.baseURI");
		Config.FAMILIES_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.families.baseURI");
		Config.DOCUMENTATIONS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.documentations.baseURI");
		Config.DOCUMENTATIONS_BASE_GRAPH = env.getProperty("fr.insee.rmes.bauhaus.documentations.baseGraph");
		Config.DOCUMENTS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.documents.baseURI");
		Config.LINKS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.links.baseURI");
		Config.DOCUMENTS_GRAPH = env.getProperty("fr.insee.rmes.bauhaus.documents.baseGraph");
		Config.DOCUMENTS_STORAGE=env.getProperty("fr.insee.rmes.bauhaus.storage.document");

		
		Config.PRODUCTS_GRAPH = env.getProperty("fr.insee.rmes.bauhaus.products.graph");
		Config.INDICATORS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.indicators.baseURI");


		Config.SESAME_SERVER_GESTION = env.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer");
		Config.REPOSITORY_ID_GESTION = env.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.repository");
		Config.BASE_URI_GESTION = env.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.baseURI");

		Config.SESAME_SERVER_PUBLICATION = env.getProperty("fr.insee.rmes.bauhaus.sesame.publication.sesameServer");
		Config.REPOSITORY_ID_PUBLICATION = env.getProperty("fr.insee.rmes.bauhaus.sesame.publication.repository");
		Config.BASE_URI_PUBLICATION = env.getProperty("fr.insee.rmes.bauhaus.sesame.publication.baseURI");

		Config.BASE_URI_METADATA_API = env.getProperty("fr.insee.rmes.bauhaus.metadata.api.baseURI");

		Config.SPOC_SERVICE_URL = env.getProperty("fr.insee.rmes.bauhaus.spoc.url");
		Config.SPOC_USER = env.getProperty("fr.insee.rmes.bauhaus.spoc.user");
		Config.SPOC_PASSWORD = env.getProperty("fr.insee.rmes.bauhaus.spoc.password");

		Config.BROKER_URL = env.getProperty("fr.insee.rmes.bauhaus.broker.url");
		Config.BROKER_USER = env.getProperty("fr.insee.rmes.bauhaus.broker.user");
		Config.BROKER_PASSWORD = env.getProperty("fr.insee.rmes.bauhaus.broker.password");

		Config.LDAP_URL = env.getProperty("fr.insee.rmes.bauhaus.ldap.url");

		Config.IGESA_URL = env.getProperty("fr.insee.rmes.bauhaus.igesa.url");
		Config.IGESA_APP_ID = env.getProperty("fr.insee.rmes.bauhaus.igesa.id");
		Config.IGESA_USER = env.getProperty("fr.insee.rmes.bauhaus.igesa.user");
		Config.IGESA_PASSWORD = env.getProperty("fr.insee.rmes.bauhaus.igesa.password");

		Config.SWAGGER_HOST = env.getProperty("fr.insee.rmes.bauhaus.api.host");
		Config.SWAGGER_BASEPATH = env.getProperty("fr.insee.rmes.bauhaus.api.basepath");
		Config.SWAGGER_URL=(Config.REQUIRES_SSL ? "https" : "http") + "://" + Config.SWAGGER_HOST + "/" + Config.SWAGGER_BASEPATH;
	}
}
