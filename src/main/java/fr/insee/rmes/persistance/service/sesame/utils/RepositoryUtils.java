package fr.insee.rmes.persistance.service.sesame.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.OpenRDFException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.Update;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.springframework.stereotype.Component;

import fr.insee.rmes.exceptions.RmesException;

@Component
public abstract class RepositoryUtils {
	
	private static final String VALUE = "value";
	private static final String BINDINGS = "bindings";
	private static final String RESULTS = "results";
	private static final String EXECUTE_QUERY_FAILED = "Execute query failed : ";
	static final Logger logger = LogManager.getLogger(RepositoryUtils.class);


	public static Repository initRepository(String sesameServer, String repositoryID) {
		Repository repo = new HTTPRepository(sesameServer, repositoryID);
		try {
			repo.initialize();
		} catch (Exception e) {
			logger.error("Initialisation de la connection à la base sesame {} impossible", sesameServer);
			logger.error(e.getMessage());
		}
		return repo;
	}
	
	public RepositoryConnection getConnection(Repository repository) throws RmesException {
		RepositoryConnection con = null;
		try {
			con = repository.getConnection();
		} catch (RepositoryException e) {
			logger.error("Connection au repository impossible : {}", repository.getDataDir());
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Connection au repository impossible : " + repository.getDataDir());		}
		return con;
	}
	
	/**
	 * Method which aims to execute a sparql update
	 * 
	 * @param updateQuery
	 * @return String
	 * @throws RmesException 
	 */
	public static Response.Status executeUpdate(String updateQuery,Repository repository) throws RmesException {
		Update update = null;
		String queryWithPrefixes = QueryUtils.PREFIXES + updateQuery;
		try {
			RepositoryConnection conn = repository.getConnection();
			update = conn.prepareUpdate(QueryLanguage.SPARQL, queryWithPrefixes);
			update.execute();
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("{} {} {}",EXECUTE_QUERY_FAILED, updateQuery, repository);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), EXECUTE_QUERY_FAILED + updateQuery);		
		}
		return(Response.Status.OK);
	}

	/**
	 * Method which aims to execute a sparql query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static String executeQuery(RepositoryConnection conn, String query) throws RmesException {
		TupleQuery tupleQuery = null;
		OutputStream stream = new ByteArrayOutputStream();
		try {
			tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.evaluate(new SPARQLResultsJSONWriter(stream));
		} catch (OpenRDFException e) {
			logger.error("{} {}",EXECUTE_QUERY_FAILED, query);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), EXECUTE_QUERY_FAILED + query);		
		}
		return stream.toString();
	}
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static String getResponse(String query, Repository repository) throws RmesException {
		String response = "";
		try {
			RepositoryConnection conn = repository.getConnection();
			String queryWithPrefixes = QueryUtils.PREFIXES + query;
			response = executeQuery(conn, queryWithPrefixes);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("{} {}",EXECUTE_QUERY_FAILED, query);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), EXECUTE_QUERY_FAILED + query);		
		}
		return response;
	}
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONArray
	 * @throws RmesException 
	 */
	public static JSONArray getResponseAsArray(String query, Repository repository) throws RmesException {
		String response = getResponse(query, repository);
		if (response.equals("")){
			return null;
		}
		JSONObject res = new JSONObject(response);
		return sparqlJSONToResultArrayValues(res);
	}
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONArray
	 * @throws RmesException 
	 */
	public static JSONArray getResponseAsJSONList(String query, Repository repository) throws RmesException {
		String response = getResponse(query, repository);
		if (response.equals("")){
			return null;
		}
		JSONObject res = new JSONObject(response);
		return sparqlJSONToResultListValues(res);
	}
	
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONObject
	 * @throws RmesException 
	 */
	public static JSONObject getResponseAsObject(String query, Repository repository) throws RmesException {
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
	 * @throws RmesException 
	 * @throws JSONException 
	 */
	public static boolean getResponseAsBoolean(String query, Repository repository) throws RmesException {
		JSONObject res = new JSONObject(getResponse(query, repository));
		return res.getBoolean("boolean");
	}

	
	public static JSONArray sparqlJSONToResultArrayValues(JSONObject jsonSparql) {
		JSONArray arrayRes = new JSONArray();
		if (jsonSparql.get(RESULTS) == null) {
			return null;
		}

		int nbRes = ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS)).length();

		for (int i = 0; i < nbRes; i++) {
			final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS))
					.get(i);
			final JSONObject jsonResults = new JSONObject();

			Set<String> set = json.keySet();
			set.forEach(s -> jsonResults.put(s, ((JSONObject) json.get(s)).get(VALUE)));
			arrayRes.put(jsonResults);
		}
		return arrayRes;
	}
	
	public static JSONArray sparqlJSONToResultListValues(JSONObject jsonSparql) {
		JSONArray arrayRes = new JSONArray();
		if (jsonSparql.get(RESULTS) == null) {
			return null;
		}

		int nbRes = ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS)).length();

		for (int i = 0; i < nbRes; i++) {
			final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS))
					.get(i);
			Set<String> set = json.keySet();
			set.forEach(s -> arrayRes.put(((JSONObject)json.get(s)).get(VALUE)));
		}
		return arrayRes;
	}
	
	
	public JSONObject sparqlJSONToValues(JSONObject jsonSparql) {
		if (jsonSparql.get(RESULTS) == null) {
			return null;
		}

		final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS))
				.get(0);
		final JSONObject jsonResults = new JSONObject();

		Set<String> set = json.keySet();
		set.forEach(s -> jsonResults.put(s, ((JSONObject) json.get(s)).get(VALUE)));
		return jsonResults;
	}
	

}
