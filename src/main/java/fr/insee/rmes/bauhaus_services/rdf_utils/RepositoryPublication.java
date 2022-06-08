package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

/**
 * Getters only get on publication base
 * Setters (publish methods and execute methods) operate on publication and internal publication base
 * @author rco0ck
 *
 */
@org.springframework.stereotype.Repository("RepositoryPublication")
public class RepositoryPublication extends RepositoryUtils{

	protected static Config config; 
	
	private static final String THREE_PARAMS_LOG = "{} {} {}";

	private static final String CONNECTION_TO = "Connection to ";

	private static final String FAILED = " failed";

	static final Logger logger = LogManager.getLogger(RepositoryPublication.class);

	private static Repository repositoryPublicationExterne ;
	private static Repository repositoryPublicationInterne ;

	@PostConstruct
	public static void init() {
		repositoryPublicationExterne = initRepository(config.getRdfServerPublication(), config.getRepositoryIdPublication());
		repositoryPublicationInterne = initRepository(config.getRdfServerPublicationInterne(), config.getRepositoryIdPublicationInterne());
	}

	

	public static void setConfig(Config config) {
		RepositoryPublication.config = config;
	}

	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static String getResponse(String query) throws RmesException {
		return getResponse(query, repositoryPublicationExterne);
	}
	
	/**
	 * Method which aims to produce response from a sparql query for internal Repository
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static String getResponseInternalPublication(String query) throws RmesException {
		return getResponse(query, repositoryPublicationInterne);
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONArray
	 * @throws RmesException 
	 */
	public static JSONArray getResponseAsArray(String query) throws RmesException {
		return getResponseAsArray(query, repositoryPublicationExterne);
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONObject
	 * @throws RmesException 
	 */
	public static JSONObject getResponseAsObject(String query) throws RmesException {
		return getResponseAsObject(query, repositoryPublicationExterne);
	}

	/**
	 * Method which aims to produce response from a sparql ASK query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 * @throws JSONException 
	 */
	public static Boolean getResponseAsBoolean(String query) throws  RmesException {
		return getResponseForAskQuery(query, repositoryPublicationExterne);
	}
	
	/**
	 * Method which aims to execute sparql update
	 * 
	 * @param updateQuery
	 * @return String
	 * @throws RmesException 
	 */
	public static HttpStatus executeUpdate(String updateQuery) throws RmesException {
		HttpStatus status = executeUpdate(updateQuery, repositoryPublicationInterne);
		if (status.is2xxSuccessful() ) {
			status = executeUpdate(updateQuery, repositoryPublicationExterne);
		}
		return status;
	}

	public static void publishConcept(Resource concept, Model model, List<Resource> noteToClear,
			List<Resource> topConceptOfToDelete) throws RmesException {
		publishConcept(concept, model, noteToClear,topConceptOfToDelete, repositoryPublicationInterne);
		publishConcept(concept, model, noteToClear,topConceptOfToDelete, repositoryPublicationExterne);
	}
	
	private static void publishConcept(Resource concept, Model model, List<Resource> noteToClear,
			List<Resource> topConceptOfToDelete, Repository repo) throws RmesException {


		if (repo == null) {return ;}

		try {
			RepositoryConnection conn = repo.getConnection();
			// notes to delete
			for (Resource note : noteToClear) {
				conn.remove(note, null, null);
			}
			// top concepts of to delete
			for (Resource c : topConceptOfToDelete) {
				conn.remove(c, SKOS.TOP_CONCEPT_OF, null);
			}
			// links to delete
			clearConceptLinks(concept, conn);

			conn.remove(concept, null, null);
			conn.add(model);
			conn.close();
			logger.info("Publication of concept : {}", concept);
		} catch (RepositoryException e) {
			logger.error("Publication of concept : {} {} {}", concept, FAILED,  e.getMessage());
			logger.error(THREE_PARAMS_LOG, CONNECTION_TO , repo, FAILED);
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + repo + FAILED);
		}
	}

	public static void publishResource(Resource resource, Model model, String type) throws RmesException {
		publishResource(resource, model, type, repositoryPublicationInterne);
		publishResource(resource, model, type, repositoryPublicationExterne);
	}

	private static void publishResource(Resource resource, Model model, String type, Repository repo) throws RmesException {
		if (repo == null) {return ;}

		try {
			RepositoryConnection conn = repo.getConnection();
			conn.remove(resource, null, null);
			conn.add(model);
			conn.close();
			logger.info("Publication of Resource {} : {}" ,type, resource);
		} catch (RepositoryException e) {
			logger.error("Publication of Resource {} : {} {}" ,type, resource, FAILED);
			logger.error(THREE_PARAMS_LOG, CONNECTION_TO, repo, FAILED);
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + repo + FAILED);
		}
	}
	
	public static void publishContext(Resource graph, Model model, String type) throws RmesException {
		publishContext(graph, model, type, repositoryPublicationInterne);
		publishContext(graph, model, type, repositoryPublicationExterne);
	}
	
	public static HttpStatus persistFile(InputStream input, RDFFormat format, String graph) throws RmesException {
		return persistFile(input, format, graph, repositoryPublicationInterne, repositoryPublicationExterne);
	}
	

	public static File getCompleteGraphInTrig(Resource context) throws RmesException {
		try (RepositoryConnection conn = repositoryPublicationExterne.getConnection() ){
			return getCompleteGraphInTrig(conn, context);
		}catch (RepositoryException e) {
			logger.warn("Can not find graph {} in external repository", context);
			try (RepositoryConnection conn = repositoryPublicationInterne.getConnection() ){
				return getCompleteGraphInTrig(conn, context);
			}catch (RepositoryException e2) {
				throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e2.getMessage(), "Failure get Graph, both in external or internal repositories : " + context);
			} 
		} 	
	}
	
	private static void publishContext(Resource context, Model model, String type, Repository repo) throws RmesException {
		if (repo == null) {return ;}

		try {
			RepositoryConnection conn = repo.getConnection();
			conn.clear(context);
			conn.add(model);
			conn.close();
			logger.info("Publication of Graph {} : {}" ,type, context);
		} catch (RepositoryException e) {
			logger.error("Publication of Graph {} : {} {}" ,type, context, FAILED);
			logger.error(THREE_PARAMS_LOG, CONNECTION_TO, repo, FAILED);
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + repo + FAILED);
		}
	}

	public static void clearStructureAndComponentForAllRepositories(Resource structure) throws RmesException {
		clearStructureAndComponents(structure, repositoryPublicationExterne);
		clearStructureAndComponents(structure, repositoryPublicationInterne);
	}

	private static void clearConceptLinks(Resource concept, RepositoryConnection conn) throws RmesException {
		List<IRI> typeOfLink = Arrays.asList(SKOS.BROADER, SKOS.NARROWER, SKOS.MEMBER, DCTERMS.REFERENCES,
				DCTERMS.REPLACES, SKOS.RELATED, DCTERMS.IS_REPLACED_BY);

		for (IRI predicat : typeOfLink) {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, concept, false);
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		}
	}
}
