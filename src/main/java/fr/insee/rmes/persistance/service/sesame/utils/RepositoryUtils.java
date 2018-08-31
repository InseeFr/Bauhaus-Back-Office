package fr.insee.rmes.persistance.service.sesame.utils;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.OpenRDFException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

public class RepositoryUtils {
	
	final static Logger logger = LogManager.getLogger(RepositoryUtils.class);


	public static Repository initRepository(String sesameServer, String repositoryID) {
		Repository repo = new HTTPRepository(sesameServer, repositoryID);
		try {
			repo.initialize();
		} catch (Exception e) {
			logger.error("Initialisation de la connection à la base sesame " + sesameServer + " impossible");
			logger.error(e.getMessage());
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
	public static String getResponse(String query, Repository repository) {
		String response = "";
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
	 * @return JSONArray
	 */
	public static JSONArray getResponseAsArray(String query, Repository repository) {
		String response = getResponse(query, repository);
		if (response.equals("")){
			return null;
		}
		JSONObject res = new JSONObject(response);
		JSONArray resArray = sparqlJSONToResultArrayValues(res);
		return resArray;
	}
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONObject
	 */
	public static JSONObject getResponseAsObject(String query, Repository repository) {
		JSONArray resArray = getResponseAsArray(query, repository);
		if (resArray==null || resArray.length() == 0) {
			return new JSONObject();
		}
		return (JSONObject) resArray.get(0);
	}
	
	
	/**
	 * Method which aims to produce response from a sparql ASK query
	 * 
	 * @param query
	 * @return String
	 */
	public static Boolean getResponseAsBoolean(String query, Repository repository) {
		JSONObject res = new JSONObject(getResponse(query, repository));
		return res.getBoolean("boolean");
	}

	
	public static JSONArray sparqlJSONToResultArrayValues(JSONObject jsonSparql) {
		JSONArray arrayRes = new JSONArray();
		if (jsonSparql.get("results") == null) {
			return null;
		}

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
		if (jsonSparql.get("results") == null) {
			return null;
		}

		final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get("results")).get("bindings"))
				.get(0);
		final JSONObject jsonResults = new JSONObject();

		Set<String> set = json.keySet();
		set.forEach(s -> jsonResults.put(s, ((JSONObject) json.get(s)).get("value")));
		return jsonResults;
	}
	
	
}
