package fr.insee.rmes.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

public class Config {

	private static final  Logger logger = LogManager.getLogger(ApplicationContext.class);
	
	public static String APP_HOST = "";

	public static String ENV = "";

	public static boolean REQUIRES_SSL = false;

	public static String DEFAULT_CONTRIBUTOR = "";
	public static String DEFAULT_MAIL_SENDER = "";
	public static String MAX_LENGTH_SCOPE_NOTE = "";

	public static String LG1 = "";
	public static String LG2 = "";

	public static String BASE_GRAPH = "";


	public static String PASSWORD_GESTIONNAIRE = "";
	public static String PASSWORD_PRODUCTEUR = "";

	public static String CONCEPTS_GRAPH = "";
	public static String CONCEPTS_SCHEME = "";
	public static String CONCEPTS_BASE_URI = "";
	public static String COLLECTIONS_BASE_URI = "";

	public static String CLASSIF_FAMILIES_GRAPH = "";

	public static String OPERATIONS_GRAPH = "";
	public static String OPERATIONS_BASE_URI = "";
	public static String OP_SERIES_BASE_URI = "";
	public static String OP_FAMILIES_BASE_URI = "";
	public static String DOCUMENTATIONS_BASE_URI = "";
	public static String DOCUMENTATIONS_GRAPH = "";
	public static String DOCUMENTS_BASE_URI = "";

	public static String MSD_GRAPH= "";
	public static String MSD_CONCEPTS_GRAPH= "";
	public static String DOCUMENTATIONS_GEO_GRAPH = "";
	public static String DOCUMENTATIONS_GEO_BASE_URI = "";

	public static String LINKS_BASE_URI = "";
	public static String DOCUMENTS_GRAPH = "";
	public static String DOCUMENTS_STORAGE = "";

	public static String PRODUCTS_GRAPH = "";

	public static String PRODUCTS_BASE_URI = "";

	// STRUCTURE
	public static String STRUCTURES_GRAPH = "";
	public static String STRUCTURES_BASE_URI = "";
	public static String STRUCTURES_COMPONENTS_GRAPH = "";
	public static String STRUCTURES_COMPONENTS_BASE_URI = "";


	public static String CODELIST_GRAPH = "";
	public static String CODE_LIST_BASE_URI = "";


	public static String ORGANIZATIONS_GRAPH = "";
	public static String ORG_INSEE_GRAPH = "";
	
	public static String GEOGRAPHY_GRAPH = "";

	public static String SESAME_SERVER_GESTION = "";
	public static String REPOSITORY_ID_GESTION = "";
	public static String BASE_URI_GESTION = "";

	public static String SESAME_SERVER_PUBLICATION = "";
	public static String REPOSITORY_ID_PUBLICATION = "";
	
	public static String SESAME_SERVER_PUBLICATION_INTERNE = "";
	public static String REPOSITORY_ID_PUBLICATION_INTERNE = "";
	
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

		//Initialize general configurations
		Config.APP_HOST = env.getProperty("fr.insee.rmes.bauhaus.appHost");

		Config.ENV = env.getProperty("fr.insee.rmes.bauhaus.env");
		Config.REQUIRES_SSL = Boolean.valueOf(env.getProperty("fr.insee.rmes.bauhaus.force.ssl"));

		Config.LG1 = env.getProperty("fr.insee.rmes.bauhaus.lg1");
		Config.LG2 = env.getProperty("fr.insee.rmes.bauhaus.lg2");

		Config.BASE_GRAPH = env.getProperty("fr.insee.rmes.bauhaus.baseGraph");

		Config.PASSWORD_GESTIONNAIRE = env.getProperty("fr.insee.rmes.bauhaus.gestionnaire.password");
		Config.PASSWORD_PRODUCTEUR = env.getProperty("fr.insee.rmes.bauhaus.producteur.password");

		Config.SESAME_SERVER_GESTION = env.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer");
		Config.REPOSITORY_ID_GESTION = env.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.repository");
		Config.BASE_URI_GESTION = env.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.baseURI");

		Config.SESAME_SERVER_PUBLICATION = env.getProperty("fr.insee.rmes.bauhaus.sesame.publication.sesameServer");
		Config.REPOSITORY_ID_PUBLICATION = env.getProperty("fr.insee.rmes.bauhaus.sesame.publication.repository");
		
		Config.SESAME_SERVER_PUBLICATION_INTERNE = env.getProperty("fr.insee.rmes.bauhaus.sesame.publication.interne.sesameServer");
		Config.REPOSITORY_ID_PUBLICATION_INTERNE = env.getProperty("fr.insee.rmes.bauhaus.sesame.publication.interne.repository");

		Config.BASE_URI_PUBLICATION = env.getProperty("fr.insee.rmes.bauhaus.sesame.publication.baseURI");


		//Initialize concepts configuration
		readConfigForConcepts(env);

		//Initialize Classifications
		Config.CLASSIF_FAMILIES_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.classifications.families.graph");

		//Initialize Operations
		readConfigForOperations(env);

		//Initialize Structures
		readConfigForStructures(env);

		//Initialize Code lists
		readConfigForCodeLists(env);

		//Initialize Organizations
		Config.ORGANIZATIONS_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.organisations.graph");
		Config.ORG_INSEE_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.insee.graph");
		
		//Initialize Geography
		Config.GEOGRAPHY_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.geographie.graph");
		
		
		//Initialize other services
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
		Config.SWAGGER_URL = (Config.REQUIRES_SSL ? "https" : "http") + "://" + Config.SWAGGER_HOST + "/" + Config.SWAGGER_BASEPATH;
	}

	private static void readConfigForCodeLists(Environment env) {
		Config.CODE_LIST_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.codeList.baseURI");
		Config.CODELIST_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.codelists.graph");
	}

	private static void readConfigForConcepts(Environment env) {
		Config.DEFAULT_CONTRIBUTOR = env.getProperty("fr.insee.rmes.bauhaus.concepts.defaultContributor");
		Config.DEFAULT_MAIL_SENDER = env.getProperty("fr.insee.rmes.bauhaus.concepts.defaultMailSender");
		Config.MAX_LENGTH_SCOPE_NOTE = env.getProperty("fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote");


		Config.CONCEPTS_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.concepts.graph");
		Config.CONCEPTS_SCHEME = env.getProperty("fr.insee.rmes.bauhaus.concepts.scheme");
		Config.CONCEPTS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.concepts.baseURI");
		Config.COLLECTIONS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.collections.baseURI");
	}

	private static void readConfigForOperations(Environment env) {
		Config.OPERATIONS_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.operations.graph");
		Config.OPERATIONS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.operations.baseURI");
		Config.OP_SERIES_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.operations.series.baseURI");
		Config.OP_FAMILIES_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.operations.families.baseURI");

		Config.DOCUMENTATIONS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.documentations.baseURI");
		Config.DOCUMENTATIONS_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.documentations.graph");
		Config.MSD_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.documentations.msd.graph");
		Config.MSD_CONCEPTS_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.documentations.concepts.graph");

		Config.DOCUMENTATIONS_GEO_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.documentation.geographie.baseURI");
		Config.DOCUMENTATIONS_GEO_GRAPH = BASE_GRAPH +  env.getProperty("fr.insee.rmes.bauhaus.documentation.geographie.graph");

		Config.DOCUMENTS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.documents.baseURI");
		Config.LINKS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.links.baseURI");
		Config.DOCUMENTS_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.documents.graph");
		Config.DOCUMENTS_STORAGE = env.getProperty("fr.insee.rmes.bauhaus.storage.document");

		Config.PRODUCTS_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.products.graph");
		Config.PRODUCTS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.products.baseURI");
	}

	private static void readConfigForStructures(Environment env) {
		Config.STRUCTURES_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.structures.graph");
		Config.STRUCTURES_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.structures.baseURI");
		Config.STRUCTURES_COMPONENTS_GRAPH = BASE_GRAPH + env.getProperty("fr.insee.rmes.bauhaus.structures.components.graph");
		Config.STRUCTURES_COMPONENTS_BASE_URI = env.getProperty("fr.insee.rmes.bauhaus.structures.components.baseURI");
	}

	public static void printMajorConfig() {
		logger.info("*********************** CONFIG USED ***********************************");

		logger.info("ENV : {}", ENV);
		logger.info("SERVEUR GESTION : {} _ REPO : {} _ BASEURI : {}",SESAME_SERVER_GESTION,REPOSITORY_ID_GESTION, BASE_URI_GESTION);
		logger.info("SERVEUR PUBLICATION : {} _ REPO : {} _ BASEURI : {}",SESAME_SERVER_PUBLICATION, REPOSITORY_ID_PUBLICATION, BASE_URI_PUBLICATION);
		logger.info("SERVEUR PUB INTERNE : {} _ REPO : {}",SESAME_SERVER_PUBLICATION_INTERNE,REPOSITORY_ID_PUBLICATION_INTERNE);
		
		logger.info("DOCUMENT STORAGE : {}", DOCUMENTS_STORAGE);
		
		logger.info("*********************** END CONFIG USED ***********************************");
		
		
	}

}