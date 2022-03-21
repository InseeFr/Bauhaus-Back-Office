package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.util.Arrays;
import java.util.List;

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
public class RepositoryPublication extends RepositoryUtils{

	private static final String THREE_PARAMS_LOG = "{} {} {}";

	private static final String CONNECTION_TO = "Connection to ";

	private static final String FAILED = " failed";

	static final Logger logger = LogManager.getLogger(RepositoryPublication.class);

	public static final Repository REPOSITORY_PUBLICATION = initRepository(Config.SESAME_SERVER_PUBLICATION, Config.REPOSITORY_ID_PUBLICATION);
	public static final Repository REPOSITORY_PUBLICATION_INTERNE = initRepository(Config.SESAME_SERVER_PUBLICATION_INTERNE, Config.REPOSITORY_ID_PUBLICATION_INTERNE);

	
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static String getResponse(String query) throws RmesException {
		return getResponse(query, REPOSITORY_PUBLICATION);
	}
	
	/**
	 * Method which aims to produce response from a sparql query for internal Repository
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static String getResponseInternalPublication(String query) throws RmesException {
		return getResponse(query, REPOSITORY_PUBLICATION_INTERNE);
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONArray
	 * @throws RmesException 
	 */
	public static JSONArray getResponseAsArray(String query) throws RmesException {
		return getResponseAsArray(query, REPOSITORY_PUBLICATION);
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONObject
	 * @throws RmesException 
	 */
	public static JSONObject getResponseAsObject(String query) throws RmesException {
		return getResponseAsObject(query, REPOSITORY_PUBLICATION);
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
		return getResponseForAskQuery(query, REPOSITORY_PUBLICATION);
	}
	
	/**
	 * Method which aims to execute sparql update
	 * 
	 * @param updateQuery
	 * @return String
	 * @throws RmesException 
	 */
	public static HttpStatus executeUpdate(String updateQuery) throws RmesException {
		HttpStatus status = executeUpdate(updateQuery, REPOSITORY_PUBLICATION_INTERNE);
		if (status.is2xxSuccessful() ) {
			status = executeUpdate(updateQuery, REPOSITORY_PUBLICATION);
		}
		return status;
	}

	public static void publishConcept(Resource concept, Model model, List<Resource> noteToClear,
			List<Resource> topConceptOfToDelete) throws RmesException {
		publishConcept(concept, model, noteToClear,topConceptOfToDelete, REPOSITORY_PUBLICATION_INTERNE);
		publishConcept(concept, model, noteToClear,topConceptOfToDelete, REPOSITORY_PUBLICATION);
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
		publishResource(resource, model, type, REPOSITORY_PUBLICATION_INTERNE);
		publishResource(resource, model, type, REPOSITORY_PUBLICATION);
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
		publishContext(graph, model, type, REPOSITORY_PUBLICATION_INTERNE);
		publishContext(graph, model, type, REPOSITORY_PUBLICATION);
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
		clearStructureAndComponents(structure, REPOSITORY_PUBLICATION);
		clearStructureAndComponents(structure, REPOSITORY_PUBLICATION_INTERNE);
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

	
	private RepositoryPublication() {
	    throw new IllegalStateException("Utility class");
	}

}
