package fr.insee.rmes.persistance.service.sesame.utils;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
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

	private static final String CONNECTION_TO = "Connection to ";

	private static final String FAILED = " failed";

	static final Logger logger = LogManager.getLogger(RepositoryPublication.class);

	public static final Repository REPOSITORY_PUBLICATION = RepositoryUtils
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
	public static Boolean getResponseAsBoolean(String query) throws  RmesException {
		return RepositoryUtils.getResponseAsBoolean(query, REPOSITORY_PUBLICATION);
	}
	
	/**
	 * Method which aims to execute sparql update
	 * 
	 * @param updateQuery
	 * @return String
	 * @throws RmesException 
	 */
	public static Response.Status executeUpdate(String updateQuery) throws RmesException {
		return RepositoryUtils.executeUpdate(updateQuery, REPOSITORY_PUBLICATION);
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
			logger.error(CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
		}
	}

	/*
	 * TODO: factoriser les methodes pour publier famille/serie/Operation/Indicateur
	 */
	
	public static void publishCollection(Resource collection, Model model) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();

			conn.remove(collection, null, null);
			conn.add(model);
			conn.close();
			logger.info("Publication of collection : " + collection);
		} catch (OpenRDFException e) {
			logger.error("Publication of collection : " + collection + FAILED);
			logger.error(CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);

		}
	}

	public static void publishFamily(Resource family, Model model) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();

			conn.remove(family, null, null);
			conn.add(model);
			conn.close();
			logger.info("Publication of family : " + family);
		} catch (OpenRDFException e) {
			logger.error("Publication of family : " + family + FAILED);
			logger.error(CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);

		}
	}
	
	public static void publishIndicator(Resource indicator, Model model) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();

			conn.remove(indicator, null, null);
			conn.add(model);
			conn.close();
			logger.info("Publication of indicator : " + indicator);
		} catch (OpenRDFException e) {
			logger.error("Publication of indicator : " + indicator + FAILED);
			logger.error(CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);

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
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "RepositoryException");
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "RepositoryException");
			}
		}
	}
	

	public static void publishSeries(Resource series, Model model) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();

			//TODO: work only in graph /operations/	?	
			conn.remove(series, null, null);
			//TODO: remove triplets ?a ?b series où ?b = hasPart,SeeAlso,Replaces,IsReplacedBy mais pas si ?b = isPartOf
			//conn.remove(null, null, series);
			
			conn.add(model);
			conn.close();
			logger.info("Publication of series : " + series);
		} catch (OpenRDFException e) {
			logger.error("Publication of series : " + series + FAILED);
			logger.error(CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);

		}
	}
		
	public static void publishOperation(Resource operation, Model model) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();

			//TODO: work only in graph /operations/	?	
			conn.remove(operation, null, null);
			//TODO: remove triplets ?a ?b operations
			
			conn.add(model);
			conn.close();
			logger.info("Publication of operation : " + operation);
		} catch (OpenRDFException e) {
			logger.error("Publication of operation : " + operation + FAILED);
			logger.error(CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);

		}
	}
	
	public static void publishMetadataReport(Resource sims, Model model) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();

			conn.clear(sims);
			conn.add(model);
			conn.close();
			logger.info("Publication of sims : " + sims);
		} catch (OpenRDFException e) {
			logger.error("Publication of sims : " + sims + FAILED);
			logger.error(CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + Config.SESAME_SERVER_PUBLICATION + FAILED);
		}
	}
	
	private RepositoryPublication() {
	    throw new IllegalStateException("Utility class");
	}

}
