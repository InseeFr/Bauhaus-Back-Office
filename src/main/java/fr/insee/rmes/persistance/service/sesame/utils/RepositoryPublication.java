package fr.insee.rmes.persistance.service.sesame.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class RepositoryPublication {

	final static Logger logger = LogManager.getLogger(RepositoryPublication.class);

	public final static Repository REPOSITORY_PUBLICATION = RepositoryUtils
			.initRepository(Config.SESAME_SERVER_PUBLICATION, Config.REPOSITORY_ID_PUBLICATION);

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static String getResponse(String query) throws RmesException {
		return RepositoryUtils.getResponse(query, REPOSITORY_PUBLICATION);
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONArray
	 * @throws RmesException 
	 */
	public static JSONArray getResponseAsArray(String query) throws RmesException {
		return RepositoryUtils.getResponseAsArray(query, REPOSITORY_PUBLICATION);
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONObject
	 * @throws RmesException 
	 */
	public static JSONObject getResponseAsObject(String query) throws RmesException {
		return RepositoryUtils.getResponseAsObject(query, REPOSITORY_PUBLICATION);
	}

	/**
	 * Method which aims to produce response from a sparql ASK query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 * @throws JSONException 
	 */
	public static Boolean getResponseAsBoolean(String query) throws JSONException, RmesException {
		return RepositoryUtils.getResponseAsBoolean(query, REPOSITORY_PUBLICATION);
	}

	public static void publishConcept(Resource concept, Model model, List<Resource> noteToClear,
			List<Resource> topConceptOfToDelete) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();
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
			logger.info("Publication of concept : " + concept);
		} catch (OpenRDFException e) {
			logger.error("Publication of concept : " + concept + " failed : " + e.getMessage());
			logger.error("Connection to " + Config.SESAME_SERVER_PUBLICATION + " failed");
			throw new RmesException(500, e.getMessage(), "Connection to " + Config.SESAME_SERVER_PUBLICATION + " failed");
		}
	}

	public static void publishCollection(Resource collection, Model model) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();

			conn.remove(collection, null, null);
			conn.add(model);
			conn.close();
			logger.info("Publication of collection : " + collection);
		} catch (OpenRDFException e) {
			logger.error("Publication of collection : " + collection + " failed");
			logger.error("Connection to " + Config.SESAME_SERVER_PUBLICATION + " failed");
			throw new RmesException(500, e.getMessage(), "Connection to " + Config.SESAME_SERVER_PUBLICATION + " failed");

		}
	}

	public static void clearConceptLinks(Resource concept, RepositoryConnection conn) throws RmesException {
		List<URI> typeOfLink = Arrays.asList(SKOS.BROADER, SKOS.NARROWER, SKOS.MEMBER, DCTERMS.REFERENCES,
				DCTERMS.REPLACES, SKOS.RELATED);

		for (URI predicat : typeOfLink) {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, concept, false);
			} catch (RepositoryException e) {
				throw new RmesException(500, e.getMessage(), "RepositoryException");
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				throw new RmesException(500, e.getMessage(), "RepositoryException");
			}
		}
	}

}
