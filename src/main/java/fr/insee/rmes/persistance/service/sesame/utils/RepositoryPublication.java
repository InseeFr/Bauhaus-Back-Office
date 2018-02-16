package fr.insee.rmes.persistance.service.sesame.utils;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;

import fr.insee.rmes.config.Config;


public class RepositoryPublication {
	
	final static Logger logger = LogManager.getLogger(RepositoryPublication.class);
	
	public final static Repository REPOSITORY_PUBLICATION = initRepository(Config.SESAME_SERVER_PUBLICATION,
			Config.REPOSITORY_ID_PUBLICATION);
	
	public static Repository initRepository(String sesameServer, String repositoryID) {
		Repository repo = new HTTPRepository(sesameServer, repositoryID);
		try {
			repo.initialize();
		} catch (RepositoryException e) {
			logger.error("Initialisation de la connection à la base sesame " + sesameServer + " impossible");
			logger.error(e.getMessage());
			e.getMessage();
		}
		return repo;
	}

	public static RepositoryConnection getConnection(Repository repository) {
		RepositoryConnection con = null;
		try {
			con = repository.getConnection();
		} catch (RepositoryException e) {
			logger.error("Connection au repository impossible : " + repository.getDataDir());
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return con;
	}

	public static String executeQuery(RepositoryConnection conn, String query) {
		TupleQuery tupleQuery = null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.evaluate(new SPARQLResultsJSONWriter(stream));
		} catch (OpenRDFException e) {
			logger.error("Execute query failed : " + query);
			logger.error(e.getMessage());
			e.getMessage();
		}
		return stream.toString();
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 */
	public static String getResponse(String query) {
		String response = "";
		Repository repository = REPOSITORY_PUBLICATION;
		try {
			RepositoryConnection conn = repository.getConnection();
			String queryWithPrefixes = QueryUtils.PREFIXES + query;
			response = executeQuery(conn, queryWithPrefixes);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Execute query failed : " + query);
			logger.error(e.getMessage());
			e.getMessage();
		}
		return response;
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 */
	public static JSONArray getResponseAsArray(String query) {
		JSONObject res = new JSONObject(getResponse(query));
		JSONArray resArray = sparqlJSONToResultArrayValues(res);
		return resArray;
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 */
	public static JSONObject getResponseAsObject(String query) {
		JSONObject res = new JSONObject(getResponse(query));
		JSONArray resArray = sparqlJSONToResultArrayValues(res);
		if (resArray.length() == 0)
			return new JSONObject();
		return (JSONObject) resArray.get(0);
	}

	/**
	 * Method which aims to produce response from a sparql ASK query
	 * 
	 * @param query
	 * @return String
	 */
	public static Boolean getResponseAsBoolean(String query) {
		JSONObject res = new JSONObject(getResponse(query));
		return res.getBoolean("boolean");
	}

	public static JSONArray sparqlJSONToResultArrayValues(JSONObject jsonSparql) {
		JSONArray arrayRes = new JSONArray();
		if (jsonSparql.get("results") == null)
			return null;

		int nbRes = ((JSONArray) ((JSONObject) jsonSparql.get("results")).get("bindings")).length();

		for (int i = 0; i < nbRes; i++) {
			final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get("results")).get("bindings"))
					.get(i);
			final JSONObject jsonResults = new JSONObject();

			Set<String> set = json.keySet();
			set.forEach(s -> jsonResults.put(s, ((JSONObject) json.get(s)).get("value")));
			arrayRes.put(jsonResults);
		}
		return arrayRes;
	}

	public static JSONObject sparqlJSONToValues(JSONObject jsonSparql) {
		if (jsonSparql.get("results") == null)
			return null;

		final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get("results")).get("bindings"))
				.get(0);
		final JSONObject jsonResults = new JSONObject();

		Set<String> set = json.keySet();
		set.forEach(s -> jsonResults.put(s, ((JSONObject) json.get(s)).get("value")));
		return jsonResults;
	}
	
	public static void publishConcept(Resource concept, Model model, List<Resource> noteToClear, List<Resource> topConceptOfToDelete) {
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
		}
	}

	public static void publishCollection(Resource collection, Model model) {
		try {
			RepositoryConnection conn = REPOSITORY_PUBLICATION.getConnection();

			conn.remove(collection, null, null);
			conn.add(model);
			conn.close();
			logger.info("Publication of collection : " + collection);
		} catch (OpenRDFException e) {
			logger.error("Publication of collection : " + collection + " failed : " + e.getMessage());
		}
	}

	public static void clearConceptLinks(Resource concept, RepositoryConnection conn) {
		List<URI> typeOfLink = Arrays.asList(SKOS.BROADER, SKOS.NARROWER, SKOS.MEMBER, DCTERMS.REFERENCES, DCTERMS.REPLACES, SKOS.RELATED);
		
		typeOfLink.forEach(predicat -> {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, concept, false);
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		});
	}

}
