package fr.insee.rmes.config;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.external_services.authentication.user_roles_manager.Sugoi;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

@Configuration
public class Config {

	private final  Logger logger = LogManager.getLogger(Config.class);
	

	/******************************************************/
	/** GLOBAL CONF 	***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.authorizationHost}")
	private String authorizationHost;

	@Value("${fr.insee.rmes.bauhaus.appHost}")
	private String appHost;

	@Value("${fr.insee.rmes.bauhaus.env}")
	private String env;

	@Value("${fr.insee.rmes.bauhaus.force.ssl}")
	private boolean requiresSsl = false;

	@Value("${fr.insee.rmes.bauhaus.lg1}")
	private String lg1;
	@Value("${fr.insee.rmes.bauhaus.lg2}")
	private String lg2;

	@Value("${fr.insee.rmes.bauhaus.baseGraph}")
	private String baseGraph;

	@Value("${fr.insee.rmes.bauhaus.api.host}")	
	private String swaggerHost;
	@Value("${fr.insee.rmes.bauhaus.api.basepath}")	//getSwaggerUrl to have the complete URL
	private String swaggerBasepath;
	

	/******************************************************/
	/** DATABASES		***********************************/
	/******************************************************/	
	@Value("http://localhost:7200")
	private String rdfServerGestion;
	@Value("bauhaus")
	private String idRepositoryGestion;
	@Value("${fr.insee.rmes.bauhaus.sesame.gestion.baseURI}")
	private String baseUriGestion;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.sesameServer}")
	private String rdfServerPublicationExt;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.repository}")
	private String idRepositoryPublicationExt;
	@Value("")
	private String rdfServerPublicationInt;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.interne.repository}")
	private String idRepositoryPublicationInt;
	@Value("${fr.insee.rmes.bauhaus.sesame.publication.baseURI}")
	private String baseUriPublication;

	/******************************************************/
	/** EXTERNAL SERVICES *********************************/
	/******************************************************/	
	//MAIL SENDER
	@Value("${fr.insee.rmes.bauhaus.spoc.url}")
	private String spocServiceUrl;
	@Value("${fr.insee.rmes.bauhaus.spoc.user}")
	private String spocUser;
	@Value("${fr.insee.rmes.bauhaus.spoc.password}")
	private String spocPassword;

	//BROKER
	@Value("${fr.insee.rmes.bauhaus.broker.url}")
	private String brokerUrl;
	@Value("${fr.insee.rmes.bauhaus.broker.user}")
	private String brokerUser;
	@Value("${fr.insee.rmes.bauhaus.broker.password}")
	private String brokerPassword;

	//AUTHENTICATION
	@Value("${fr.insee.rmes.bauhaus.ldap.url}")
	private String ldapUrl;
	@Value("${jwt.stamp-claim}")
	private String stampClaim;
	@Value("${jwt.role-claim}")
	private String roleClaim;

	//LDAP
	@Value("")
	private String sugoiUrl;
	@Value("")
	private String sugoiUser;
	@Value("")
	private String sugoiPassword;
	@Value("")
	private String sugoiApp;
	@Value("")
	private String sugoiRealm;

	
	/******************************************************/
	/** CONCEPTS 		***********************************/
	/******************************************************/	
	
	@Value("${fr.insee.rmes.bauhaus.concepts.defaultContributor}")
	private String defaultContributor;
	@Value("${fr.insee.rmes.bauhaus.concepts.defaultMailSender}")
	private String defaultMailSender;
	@Value("${fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote}")
	private String maxLengthScopeNote;
	
	@Value("${fr.insee.rmes.bauhaus.concepts.graph}") //Getter with baseGraph
	private String conceptsGraph;
	@Value("${fr.insee.rmes.bauhaus.concepts.scheme}")
	private String conceptsScheme;
	@Value("${fr.insee.rmes.bauhaus.concepts.baseURI}")
	private String conceptsBaseUri;
	@Value("${fr.insee.rmes.bauhaus.collections.baseURI}")
	private String collectionsBaseUri;
	

	/******************************************************/
	/** CLASSIFICATIONS	***********************************/
	/******************************************************/	
	@Value("${fr.insee.rmes.bauhaus.classifications.families.graph}")	 //Getter with baseGraph
	private String classifFamiliesGraph  ;
	
	/******************************************************/
	/** OPERATIONS		***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.operations.graph}")	//Getter with baseGraph
	private String operationsGraph;
	@Value("${fr.insee.rmes.bauhaus.operations.baseURI}")	
	private String operationsBaseUri;
	@Value("${fr.insee.rmes.bauhaus.operations.series.baseURI}")	
	private String opSeriesBaseUri;
	@Value("${fr.insee.rmes.bauhaus.operations.families.baseURI}")	
	private String opFamiliesBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documentations.baseURI}")	
	private String documentationsBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documentations.graph}")	//Getter with baseGraph
	private String documentationsGraph;
	@Value("${fr.insee.rmes.bauhaus.documentations.msd.graph}")	//Getter with baseGraph
	private String msdGraph;
	@Value("${fr.insee.rmes.bauhaus.documentations.concepts.graph}")	//Getter with baseGraph
	private String msdConceptsGraph;
	@Value("${fr.insee.rmes.bauhaus.documentation.geographie.graph}")	//Getter with baseGraph
	private String documentationsGeoGraph;
	@Value("${fr.insee.rmes.bauhaus.documentation.geographie.baseURI}")	
	private String documentationsGeoBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documentation.titlePrefixLg1}")	
	private String documentationsTitlePrefixLg1;
	@Value("${fr.insee.rmes.bauhaus.documentation.titlePrefixLg2}")	
	private String documentationsTitlePrefixLg2;
	@Value("${fr.insee.rmes.bauhaus.links.baseURI}")	
	private String linksBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documents.baseURI}")	
	private String documentsBaseUri;
	@Value("${fr.insee.rmes.bauhaus.documents.graph}")	//Getter with baseGraph
	private String documentsGraph;
	@Value("${fr.insee.rmes.bauhaus.storage.document.gestion}")	
	private String documentsStorageGestion;
	@Value("${fr.insee.rmes.bauhaus.storage.document.publication}")	
	private String documentsStoragePublicationExt;
	@Value("${fr.insee.rmes.bauhaus.storage.document.publication.interne}")	
	private String documentsStoragePublicationInt;
	@Value("${fr.insee.web4g.baseURL}")	
	private String documentsBaseUrl;
	
	@Value("${fr.insee.rmes.bauhaus.products.graph}")//Getter with baseGraph
	private String productsGraph;
	@Value("${fr.insee.rmes.bauhaus.products.baseURI}")	
	private String productsBaseUri;

	/******************************************************/
	/** STRUCTURES		***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.structures.graph}")	//Getter with baseGraph
	private String structuresGraph;
	@Value("${fr.insee.rmes.bauhaus.structures.baseURI}")	
	private String structuresBaseUri;
	@Value("${fr.insee.rmes.bauhaus.structures.components.graph}")	//Getter with baseGraph
	private String structuresComponentsGraph;
	@Value("${fr.insee.rmes.bauhaus.structures.components.baseURI}")	
	private String structuresComponentsBaseUri;

	/******************************************************/
	/** CODE LISTS		***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.codelists.graph}")	//Getter with baseGraph
	private String codeListsGraph;
	@Value("${fr.insee.rmes.bauhaus.codeList.baseURI}")	
	private String codeListsBaseUri;
	
	/******************************************************/
	/** ORGANIZATIONS	***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.organisations.graph}") //Getter with baseGraph
	private String organizationsGraph;
	@Value("${fr.insee.rmes.bauhaus.insee.graph}") //Getter with baseGraph
	private String orgInseeGraph;
	
	
	/******************************************************/
	/** GEOGRAPHY		***********************************/
	/******************************************************/
	@Value("${fr.insee.rmes.bauhaus.geographie.graph}")	 //Getter with baseGraph
	private String geographyGraph;

	


	/******************************************************/
	/** PRINTER			***********************************/
	/******************************************************/
	public void printMajorConfig() {
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

	
	public Config() {
		//constructor for spring
	}
	
	/******************************************************/
	/** INIT STATIC		***********************************/
	/******************************************************/
	@PostConstruct
	private void init() {
		GenericQueries.setConfig(this);
		RdfUtils.setConfig(this);
		Sugoi.setConfig(this);
		PublicationUtils.setConfig(this);
		RepositoryPublication.setConfig(this);
	}

	/******************************************************/
	/** GETTERS 		***********************************/
	/******************************************************/

	public String getAppHost() {
		return appHost;
	}

	public String getAuthorizationHost() {
		return authorizationHost;
	}

	public String getEnv() {
		return env;
	}

	public boolean isRequiresSsl() {
		return requiresSsl;
	}

	public String getLg1() {
		return lg1;
	}

	public String getLg2() {
		return lg2;
	}

	public String getBaseGraph() {
		return baseGraph;
	}

	public String getRdfServerGestion() {
		return rdfServerGestion;
	}

	public String getRepositoryIdGestion() {
		return idRepositoryGestion;
	}

	public String getBaseUriGestion() {
		return baseUriGestion;
	}

	public String getRdfServerPublication() {
		return rdfServerPublicationExt;
	}

	public String getRepositoryIdPublication() {
		return idRepositoryPublicationExt;
	}

	public String getRdfServerPublicationInterne() {
		return rdfServerPublicationInt;
	}

	public String getRepositoryIdPublicationInterne() {
		return idRepositoryPublicationInt;
	}

	public String getBaseUriPublication() {
		return baseUriPublication;
	}

	public String getDefaultContributor() {
		return defaultContributor;
	}

	public String getDefaultMailSender() {
		return defaultMailSender;
	}

	public String getMaxLengthScopeNote() {
		return maxLengthScopeNote;
	}

	public String getConceptsGraph() {
		return baseGraph + conceptsGraph;
	}

	public String getConceptsScheme() {
		return conceptsScheme;
	}

	public String getConceptsBaseUri() {
		return conceptsBaseUri;
	}

	public String getCollectionsBaseUri() {
		return collectionsBaseUri;
	}

	public String getClassifFamiliesGraph() {
		return baseGraph + classifFamiliesGraph;
	}

	public String getOperationsGraph() {
		return baseGraph + operationsGraph;
	}

	public String getOperationsBaseUri() {
		return operationsBaseUri;
	}

	public String getOpSeriesBaseUri() {
		return opSeriesBaseUri;
	}

	public String getOpFamiliesBaseUri() {
		return opFamiliesBaseUri;
	}

	public String getDocumentationsBaseUri() {
		return documentationsBaseUri;
	}

	public String getDocumentationsGraph() {
		return baseGraph + documentationsGraph;
	}

	public String getDocumentsBaseUri() {
		return documentsBaseUri;
	}

	public String getMsdGraph() {
		return baseGraph + msdGraph;
	}

	public String getMsdConceptsGraph() {
		return baseGraph + msdConceptsGraph;
	}

	public String getDocumentationsGeoGraph() {
		return baseGraph + documentationsGeoGraph;
	}

	public String getDocumentationsGeoBaseUri() {
		return documentationsGeoBaseUri;
	}

	public String getDocumentationsTitlePrefixLg1() {
		return documentationsTitlePrefixLg1;
	}

	public String getDocumentationsTitlePrefixLg2() {
		return documentationsTitlePrefixLg2;
	}

	public String getLinksBaseUri() {
		return linksBaseUri;
	}

	public String getDocumentsGraph() {
		return baseGraph + documentsGraph;
	}

	public String getDocumentsStorageGestion() {
		return documentsStorageGestion;
	}

	public String getDocumentsStoragePublicationExterne() {
		return documentsStoragePublicationExt;
	}

	public String getDocumentsStoragePublicationInterne() {
		return documentsStoragePublicationInt;
	}

	public String getDocumentsBaseurl() {
		return documentsBaseUrl.trim();
	}

	public String getProductsGraph() {
		return baseGraph + productsGraph;
	}

	public String getProductsBaseUri() {
		return productsBaseUri;
	}

	public String getStructuresGraph() {
		return baseGraph + structuresGraph;
	}

	public String getStructuresBaseUri() {
		return structuresBaseUri;
	}

	public String getStructuresComponentsGraph() {
		return baseGraph + structuresComponentsGraph;
	}

	public String getStructuresComponentsBaseUri() {
		return structuresComponentsBaseUri;
	}

	public String getCodeListGraph() {
		return baseGraph + codeListsGraph;
	}

	public String getCodeListBaseUri() {
		return codeListsBaseUri;
	}

	public String getOrganizationsGraph() {
		return baseGraph + organizationsGraph;
	}

	public String getOrgInseeGraph() {
		return baseGraph + orgInseeGraph;
	}

	public String getGeographyGraph() {
		return  baseGraph + geographyGraph;
	}

	public String getSpocServiceUrl() {
		return spocServiceUrl;
	}

	public String getSpocUser() {
		return spocUser;
	}

	public String getSpocPassword() {
		return spocPassword;
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public String getBrokerUser() {
		return brokerUser;
	}

	public String getBrokerPassword() {
		return brokerPassword;
	}

	public String getLdapUrl() {
		return ldapUrl;
	}

	public String getStampclaim() {
		return stampClaim;
	}

	public String getRoleclaim() {
		return roleClaim;
	}

	public String getSugoiUrl() {
		return sugoiUrl;
	}

	public String getSugoiUser() {
		return sugoiUser;
	}

	public String getSugoiPassword() {
		return sugoiPassword;
	}

	public String getSugoiApp() {
		return sugoiApp;
	}

	public String getSugoiRealm() {
		return sugoiRealm;
	}

	public String getSwaggerHost() {
		return swaggerHost;
	}

	public String getSwaggerBasepath() {
		return swaggerBasepath;
	}

	public String getSwaggerUrl() {
		return (requiresSsl ? "https" : "http") + "://" + swaggerHost + "/" + swaggerBasepath;
	}

}