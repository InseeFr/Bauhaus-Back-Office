package fr.insee.rmes.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

public class Config {

	private static final  Logger logger = LogManager.getLogger(Config.class);

	/******************************************************/
	/** GLOBAL CONF 	***********************************/
	/******************************************************/	
	
	@Value("${fr.insee.rmes.bauhaus.appHost}")
	private static String appHost;

	@Value("${fr.insee.rmes.bauhaus.env}")
	private static String env;

	@Value("${fr.insee.rmes.bauhaus.force.ssl}")
	private static final boolean REQUIRES_SSL = false;

	@Value("${fr.insee.rmes.bauhaus.lg1}")
	private static String lg1;
	@Value("${fr.insee.rmes.bauhaus.lg2}")
	private static String lg2;

	@Value("${fr.insee.rmes.bauhaus.baseGraph}")
	private static String baseGraph;

	@Value("${fr.insee.rmes.bauhaus.gestionnaire.password}")
	private static String gestionnairePassword;
	@Value("${fr.insee.rmes.bauhaus.producteur.password}")
	private static String producteurPassword;
	
	@Value("${fr.insee.rmes.bauhaus.api.host}")	
	private static String swaggerHost;
	@Value("${fr.insee.rmes.bauhaus.api.basepath}")	//getSwaggerUrl to have the complete URL
	private static String swaggerBasepath;
	

	/******************************************************/
	/** DATABASES		***********************************/
	/******************************************************/	
	@Value("${fr.insee.rmes.bauhaus.sesame.gestion.sesameServer}")
	private static String rdfServerGestion;
	@Value("${fr.insee.rmes.bauhaus.sesame.gestion.repository}")
	private static String idRepositoryGestion;
	@Value("${fr.insee.rmes.bauhaus.sesame.gestion.baseURI}")
	private static String baseUriGestion;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.sesameServer}")
	private static String rdfServerPublicationExt;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.repository}")
	private static String idRepositoryPublicationExt;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.interne.sesameServer}")
	private static String rdfServerPublicationInt;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.interne.repository}")
	private static String idRepositoryPublicationInt;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.baseURI}")
	private static String baseUriPublication;

	/******************************************************/
	/** EXTERNAL SERVICES *********************************/
	/******************************************************/	
	//MAIL SENDER
	@Value("${fr.insee.rmes.bauhaus.spoc.url}")
	private static String spocServiceUrl;
	@Value("${fr.insee.rmes.bauhaus.spoc.user}")
	private static String spocUser;
	@Value("${fr.insee.rmes.bauhaus.spoc.password}")
	private static String spocPassword;

	//BROKER
	@Value("${fr.insee.rmes.bauhaus.broker.url}")
	private static String brokerUrl;
	@Value("${fr.insee.rmes.bauhaus.broker.user}")
	private static String brokerUser;
	@Value("${fr.insee.rmes.bauhaus.broker.password}")
	private static String brokerPassword;

	//AUTHENTICATION
	@Value("${fr.insee.rmes.bauhaus.ldap.url}")
	private static String ldapUrl;
	@Value("${jwt.stamp-claim}")
	private static String stampClaim;
	@Value("${jwt.role-claim}")
	private static String roleClaim;

	//LDAP
	@Value("${fr.insee.rmes.bauhaus.sugoi.url}")
	private static String sugoiUrl;
	@Value("${fr.insee.rmes.bauhaus.sugoi.id}")
	private static String sugoiUser;
	@Value("${fr.insee.rmes.bauhaus.sugoi.password}")
	private static String sugoiPassword;
	@Value("${fr.insee.rmes.bauhaus.sugoi.application}")
	private static String sugoiApp;
	@Value("${fr.insee.rmes.bauhaus.sugoi.realm}")
	private static String sugoiRealm;

	
	/******************************************************/
	/** CONCEPTS 		***********************************/
	/******************************************************/	
	
	@Value("${fr.insee.rmes.bauhaus.concepts.defaultContributor}")
	private static String defaultContributor;
	@Value("${fr.insee.rmes.bauhaus.concepts.defaultMailSender}")
	private static String defaultMailSender;
	@Value("${fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote}")
	private static String maxLengthScopeNote;
	
	@Value("${fr.insee.rmes.bauhaus.concepts.graph}") //Getter with baseGraph
	private static String conceptsGraph;
	@Value("${fr.insee.rmes.bauhaus.concepts.scheme}")
	private static String conceptsScheme;
	@Value("${fr.insee.rmes.bauhaus.concepts.baseURI}")
	private static String conceptsBaseUri;
	@Value("${fr.insee.rmes.bauhaus.collections.baseURI}")
	private static String collectionsBaseUri;
	

	/******************************************************/
	/** CLASSIFICATIONS	***********************************/
	/******************************************************/	
	@Value("${fr.insee.rmes.bauhaus.classifications.families.graph}")	 //Getter with baseGraph
	private static String classifFamiliesGraph  ;
	
	/******************************************************/
	/** OPERATIONS		***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.operations.graph}")	//Getter with baseGraph
	private static String operationsGraph;
	@Value("${fr.insee.rmes.bauhaus.operations.baseURI}")	
	private static String operationsBaseUri;
	@Value("${fr.insee.rmes.bauhaus.operations.series.baseURI}")	
	private static String opSeriesBaseUri;
	@Value("${fr.insee.rmes.bauhaus.operations.families.baseURI}")	
	private static String opFamiliesBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documentations.baseURI}")	
	private static String documentationsBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documentations.graph}")	//Getter with baseGraph
	private static String documentationsGraph;
	@Value("${fr.insee.rmes.bauhaus.documentations.msd.graph}")	//Getter with baseGraph
	private static String msdGraph;
	@Value("${fr.insee.rmes.bauhaus.documentations.concepts.graph}")	//Getter with baseGraph
	private static String msdConceptsGraph;
	@Value("${fr.insee.rmes.bauhaus.documentation.geographie.graph}")	//Getter with baseGraph
	private static String documentationsGeoGraph;
	@Value("${fr.insee.rmes.bauhaus.documentation.geographie.baseURI}")	
	private static String documentationsGeoBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documentation.titlePrefixLg1}")	
	private static String documentationsTitlePrefixLg1;
	@Value("${fr.insee.rmes.bauhaus.documentation.titlePrefixLg2}")	
	private static String documentationsTitlePrefixLg2;
	@Value("${fr.insee.rmes.bauhaus.links.baseURI}")	
	private static String linksBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documents.baseURI}")	
	private static String documentsBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documents.graph}")	//Getter with baseGraph
	private static String documentsGraph;
	@Value("${fr.insee.rmes.bauhaus.storage.document.gestion}")	
	private static String documentsStorageGestion;
	@Value("${fr.insee.rmes.bauhaus.storage.document.publication}")	
	private static String documentsStoragePublicationExt;
	@Value("${fr.insee.rmes.bauhaus.storage.document.publication.interne}")	
	private static String documentsStoragePublicationInt;
	@Value("${fr.insee.web4g.baseURL}")	
	private static String documentsBaseUrl;
	
	@Value("${fr.insee.rmes.bauhaus.products.graph}")//Getter with baseGraph
	private static String productsGraph;
	@Value("${fr.insee.rmes.bauhaus.products.baseURI}")	
	private static String productsBaseUri;

	/******************************************************/
	/** STRUCTURES		***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.structures.graph}")	//Getter with baseGraph
	private static String structuresGraph;
	@Value("${fr.insee.rmes.bauhaus.structures.baseURI}")	
	private static String structuresBaseUri;
	@Value("${fr.insee.rmes.bauhaus.structures.components.graph}")	//Getter with baseGraph
	private static String structuresComponentsGraph;
	@Value("${fr.insee.rmes.bauhaus.structures.components.baseURI}")	
	private static String structuresComponentsBaseUri;

	/******************************************************/
	/** CODE LISTS		***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.codelists.graph}")	//Getter with baseGraph
	private static String codeListsGraph;
	@Value("${fr.insee.rmes.bauhaus.codeList.baseURI}")	
	private static String codeListsBaseUri;
	
	/******************************************************/
	/** ORGANIZATIONS	***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.organisations.graph}") //Getter with baseGraph
	private static String organizationsGraph;
	@Value("${fr.insee.rmes.bauhaus.insee.graph}") //Getter with baseGraph
	private static String orgInseeGraph;
	
	
	/******************************************************/
	/** GEOGRAPHY		***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.geographie.graph}")	 //Getter with baseGraph
	private static String geographyGraph;

	


	/******************************************************/
	/** PRINTER			***********************************/
	/******************************************************/
	public static void printMajorConfig() {
		logger.info("*********************** CONFIG USED ***********************************");

		logger.info("ENV : {}", env);
		
		logger.info("SERVEUR RDF : ");
		
		logger.info("   GESTION : {} _ REPO : {} _ BASEURI : {}",rdfServerGestion,idRepositoryGestion, baseUriGestion);
		logger.info("   PUB EXTERNE : {} _ REPO : {} _ BASEURI : {}",rdfServerPublicationExt, idRepositoryPublicationExt, baseUriPublication);
		logger.info("   PUB INTERNE : {} _ REPO : {}",rdfServerPublicationInt,idRepositoryPublicationInt);
		
		logger.info("DOCUMENT STORAGE : ");
		
		logger.info("   GESTION : {}", documentsStorageGestion);
		logger.info("   PUB EXTERNE : {}", documentsStoragePublicationExt);
		logger.info("   PUB INTERNE : {}", documentsStoragePublicationInt);


		
		logger.info("*********************** END CONFIG USED ***********************************");
		
		
	}

	
	private Config() {
		throw new IllegalStateException("Utility class");
	}

	/******************************************************/
	/** GETTERS 		***********************************/
	/******************************************************/

	public static String getAppHost() {
		return appHost;
	}

	public static String getEnv() {
		return env;
	}

	public static boolean isRequiresSsl() {
		return REQUIRES_SSL;
	}

	public static String getLg1() {
		return lg1;
	}

	public static String getLg2() {
		return lg2;
	}

	public static String getBaseGraph() {
		return baseGraph;
	}

	public static String getPasswordGestionnaire() {
		return gestionnairePassword;
	}

	public static String getPasswordProducteur() {
		return producteurPassword;
	}

	public static String getSesameServerGestion() {
		return rdfServerGestion;
	}

	public static String getRepositoryIdGestion() {
		return idRepositoryGestion;
	}

	public static String getBaseUriGestion() {
		return baseUriGestion;
	}

	public static String getSesameServerPublication() {
		return rdfServerPublicationExt;
	}

	public static String getRepositoryIdPublication() {
		return idRepositoryPublicationExt;
	}

	public static String getSesameServerPublicationInterne() {
		return rdfServerPublicationInt;
	}

	public static String getRepositoryIdPublicationInterne() {
		return idRepositoryPublicationInt;
	}

	public static String getBaseUriPublication() {
		return baseUriPublication;
	}

	public static String getDefaultContributor() {
		return defaultContributor;
	}

	public static String getDefaultMailSender() {
		return defaultMailSender;
	}

	public static String getMaxLengthScopeNote() {
		return maxLengthScopeNote;
	}

	public static String getConceptsGraph() {
		return baseGraph + conceptsGraph;
	}

	public static String getConceptsScheme() {
		return conceptsScheme;
	}

	public static String getConceptsBaseUri() {
		return conceptsBaseUri;
	}

	public static String getCollectionsBaseUri() {
		return collectionsBaseUri;
	}

	public static String getClassifFamiliesGraph() {
		return baseGraph + classifFamiliesGraph;
	}

	public static String getOperationsGraph() {
		return baseGraph + operationsGraph;
	}

	public static String getOperationsBaseUri() {
		return operationsBaseUri;
	}

	public static String getOpSeriesBaseUri() {
		return opSeriesBaseUri;
	}

	public static String getOpFamiliesBaseUri() {
		return opFamiliesBaseUri;
	}

	public static String getDocumentationsBaseUri() {
		return documentationsBaseUri;
	}

	public static String getDocumentationsGraph() {
		return baseGraph + documentationsGraph;
	}

	public static String getDocumentsBaseUri() {
		return documentsBaseUri;
	}

	public static String getMsdGraph() {
		return baseGraph + msdGraph;
	}

	public static String getMsdConceptsGraph() {
		return baseGraph + msdConceptsGraph;
	}

	public static String getDocumentationsGeoGraph() {
		return baseGraph + documentationsGeoGraph;
	}

	public static String getDocumentationsGeoBaseUri() {
		return documentationsGeoBaseUri;
	}

	public static String getDocumentationsTitlePrefixLg1() {
		return documentationsTitlePrefixLg1;
	}

	public static String getDocumentationsTitlePrefixLg2() {
		return documentationsTitlePrefixLg2;
	}

	public static String getLinksBaseUri() {
		return linksBaseUri;
	}

	public static String getDocumentsGraph() {
		return baseGraph + documentsGraph;
	}

	public static String getDocumentsStorageGestion() {
		return documentsStorageGestion;
	}

	public static String getDocumentsStoragePublicationExterne() {
		return documentsStoragePublicationExt;
	}

	public static String getDocumentsStoragePublicationInterne() {
		return documentsStoragePublicationInt;
	}

	public static String getDocumentsBaseurl() {
		return documentsBaseUrl.trim();
	}

	public static String getProductsGraph() {
		return baseGraph + productsGraph;
	}

	public static String getProductsBaseUri() {
		return productsBaseUri;
	}

	public static String getStructuresGraph() {
		return baseGraph + structuresGraph;
	}

	public static String getStructuresBaseUri() {
		return structuresBaseUri;
	}

	public static String getStructuresComponentsGraph() {
		return baseGraph + structuresComponentsGraph;
	}

	public static String getStructuresComponentsBaseUri() {
		return structuresComponentsBaseUri;
	}

	public static String getCodeListGraph() {
		return baseGraph + codeListsGraph;
	}

	public static String getCodeListBaseUri() {
		return codeListsBaseUri;
	}

	public static String getOrganizationsGraph() {
		return baseGraph + organizationsGraph;
	}

	public static String getOrgInseeGraph() {
		return baseGraph + orgInseeGraph;
	}

	public static String getGeographyGraph() {
		return  baseGraph + geographyGraph;
	}

	public static String getSpocServiceUrl() {
		return spocServiceUrl;
	}

	public static String getSpocUser() {
		return spocUser;
	}

	public static String getSpocPassword() {
		return spocPassword;
	}

	public static String getBrokerUrl() {
		return brokerUrl;
	}

	public static String getBrokerUser() {
		return brokerUser;
	}

	public static String getBrokerPassword() {
		return brokerPassword;
	}

	public static String getLdapUrl() {
		return ldapUrl;
	}

	public static String getStampclaim() {
		return stampClaim;
	}

	public static String getRoleclaim() {
		return roleClaim;
	}

	public static String getSugoiUrl() {
		return sugoiUrl;
	}

	public static String getSugoiUser() {
		return sugoiUser;
	}

	public static String getSugoiPassword() {
		return sugoiPassword;
	}

	public static String getSugoiApp() {
		return sugoiApp;
	}

	public static String getSugoiRealm() {
		return sugoiRealm;
	}

	public static String getSwaggerHost() {
		return swaggerHost;
	}

	public static String getSwaggerBasepath() {
		return swaggerBasepath;
	}

	public static String getSwaggerUrl() {
		return (REQUIRES_SSL ? "https" : "http") + "://" + swaggerHost + "/" + swaggerBasepath;
	}

}