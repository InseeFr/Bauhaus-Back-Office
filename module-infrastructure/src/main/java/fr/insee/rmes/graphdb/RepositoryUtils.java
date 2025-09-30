package fr.insee.rmes.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.graphdb.exceptions.DatabaseQueryException;
import fr.insee.rmes.keycloak.KeycloakServices;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.trig.TriGParser;
import org.eclipse.rdf4j.rio.trig.TriGWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class RepositoryUtils {
	
	private static final String BINDINGS = "bindings";
	private static final String RESULTS = "results";
	private static final String EXECUTE_QUERY_FAILED = "Execute query failed : ";
	
	static final Logger logger = LoggerFactory.getLogger(RepositoryUtils.class);
	private final RepositoryInitiator repositoryInitiator;


	public RepositoryUtils(KeycloakServices keycloakServices, @Value("${fr.insee.rmes.bauhaus.rdf.auth}")RepositoryInitiator.Type type){
		repositoryInitiator=RepositoryInitiator.newInstance(type, keycloakServices);
	}

	public Repository initRepository(String rdfServer, String repositoryID) {
		if (rdfServer==null|| rdfServer.isEmpty()) {
            logger.warn("rdfServer ({}) et repositoryID ({}) ne doivent pas être nuls dans RepositoryUtils.initRepository", rdfServer, repositoryID);
			return null;
		}
		Repository repository=null;
		try{
			repository= this.repositoryInitiator.initRepository(rdfServer, repositoryID);
		} catch(Exception e) {
            logger.error("Initialisation de la connection à la base RDF {} impossible", rdfServer, e);
		}
		return repository;
	}


	public RepositoryConnection getConnection(Repository repository) throws RmesException {
		RepositoryConnection con;
		try {
			con = repository.getConnection();
		} catch (RepositoryException e) {
			logger.error("Connection au repository impossible : {}", repository.getDataDir());
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Connection au repository impossible : " + repository.getDataDir());		}
		return con;
	}
	
	/**
	 * Method which aims to execute a sparql update
	 * 
	 * @param updateQuery
	 * @return String
	 * @throws RmesException 
	 */
	public static HttpStatus executeUpdate(String updateQuery,Repository repository) throws RmesException {
		if (repository == null) {return HttpStatus.EXPECTATION_FAILED;}
		Update update;
		String queryWithPrefixes = QueryUtils.PREFIXES + updateQuery;
		try {
			RepositoryConnection conn = repository.getConnection();
			update = conn.prepareUpdate(QueryLanguage.SPARQL, queryWithPrefixes);
			update.execute();
			conn.close();
			logTrace("Repo {} --- Executed update --- \n{}", repository, queryWithPrefixes);
		} catch (RepositoryException e) {
			logger.error("{} {} {}",EXECUTE_QUERY_FAILED, updateQuery, repository);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), EXECUTE_QUERY_FAILED + updateQuery);		
		}
		return(HttpStatus.OK);
	}

	private static void logTrace(String message, Repository repository, String queryWithPrefixes) {
		if (logger.isTraceEnabled()){
			var repoUrl=repository instanceof HTTPRepository httpRepository ? httpRepository.getRepositoryURL():"unknown ("+repository.getClass()+")";
			logger.trace(message, repoUrl, queryWithPrefixes);
		}
	}


	public RepositoryResult<Statement> getCompleteGraph(RepositoryConnection con, Resource context) throws RmesException {
		RepositoryResult<Statement> statements;
		try {
			statements = con.getStatements(null, null, null,context); //get the complete Graph
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Failure get following graph : " + context);
		}
		return statements;
	}



	/**
	 * Method which aims to execute a sparql query
	 * 
	 * @param query
	 * @return String
     */
	public static String executeQuery(RepositoryConnection conn, String query) throws DatabaseQueryException {
		TupleQuery tupleQuery;

		String result;
		try {
			var stream = new ByteArrayOutputStream();
			tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.evaluate(new SPARQLResultsJSONWriter(stream));
			result= stream.toString();
			traceLogResult(conn, query, result);
		} catch (RDF4JException e) {
			logAndThrowError(query, e);
			result="";
		}
		return result;
	}

	private static void traceLogResult(RepositoryConnection conn, String query, String result) {
		logTrace("Repo {} --- Executed query --- \n{}", conn.getRepository(), query);
		logger.trace("--- Results ---\n{}", result);
	}

	/**
	 * Method which aims to execute a sparql ASK query
	 * 
	 * @param query
	 * @return String
     */
	public static boolean executeAskQuery(RepositoryConnection conn, String query) throws DatabaseQueryException {
		BooleanQuery tupleQuery;
		try {
			tupleQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query);
			var result =  tupleQuery.evaluate();
			traceLogResult(conn, query, Boolean.toString(result));
			return result;
		} catch (RDF4JException e) {
			logAndThrowError(query, e);		
		}
		return false;
	}
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
     */
	public static String getResponse(String query, Repository repository) throws DatabaseQueryException {
		String response = "";
		try {
			RepositoryConnection conn = repository.getConnection();
			String queryWithPrefixes = QueryUtils.PREFIXES + query;
			response = executeQuery(conn, queryWithPrefixes);
			conn.close();
		} catch (RDF4JException e) {
			logAndThrowError(query, e);		
		}
		return response;
	}

	private static void logAndThrowError(String query, RDF4JException e) throws DatabaseQueryException {
		throw new DatabaseQueryException(e, query);
	}
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
     */
	public static boolean getResponseForAskQuery(String query, Repository repository) throws DatabaseQueryException {
		boolean response = false;
		try {
			RepositoryConnection conn = repository.getConnection();
			String queryWithPrefixes = QueryUtils.PREFIXES + query;
			response = executeAskQuery(conn, queryWithPrefixes);
			conn.close();
		} catch (RDF4JException e) {
			logAndThrowError(query, e);		
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
		if (response.isEmpty()){
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
		if (response.isEmpty()){
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
		if (resArray==null || resArray.isEmpty()) {
			return new JSONObject();
		}
		return (JSONObject) resArray.get(0);
	}
	
	/**
	 * Return a JsonArray containing a list of jsonobject (key value)
	 * @param jsonSparql
	 * @return
	 */
	public static JSONArray sparqlJSONToResultArrayValues(JSONObject jsonSparql) {
		JSONArray arrayRes = new JSONArray();
		if (!jsonSparql.has(RESULTS) || jsonSparql.get(RESULTS) == null) {
			return null;
		}

		int nbRes = ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS)).length();

		for (int i = 0; i < nbRes; i++) {
			final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS))
					.get(i);
			final JSONObject jsonResults = new JSONObject();

			Set<String> set = json.keySet();
			set.forEach(s -> jsonResults.put(s, ((JSONObject) json.get(s)).get("value")));
			arrayRes.put(jsonResults);
		}
		return arrayRes;
	}
	
	/**
	 * Return a JsonArray containing a list of string (without key)
	 * @param jsonSparql
	 * @return
	 */
	public static JSONArray sparqlJSONToResultListValues(JSONObject jsonSparql) {
		JSONArray arrayRes = new JSONArray();
		if (!jsonSparql.has(RESULTS) || jsonSparql.get(RESULTS) == null) {
			return null;
		}

		int nbRes = ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS)).length();

		for (int i = 0; i < nbRes; i++) {
			final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS))
					.get(i);
			Set<String> set = json.keySet();
			set.forEach(s -> arrayRes.put(((JSONObject)json.get(s)).get("value")));
		}
		return arrayRes;
	}

	
	public static void clearStructureAndComponents(Resource structure, Repository repository) throws RmesException {
		List<Resource> toRemove = new ArrayList<>();
		try (RepositoryConnection conn = repository.getConnection()){
			RepositoryResult<Statement> nodes;
			RepositoryResult<Statement> specifications;
			nodes = conn.getStatements(structure, QB.COMPONENT, null, false);
			while (nodes.hasNext()) {
				Resource node = (Resource) nodes.next().getObject();
				toRemove.add(node);
				specifications = conn.getStatements(node, QB.COMPONENT, null, false);
				while (specifications.hasNext()) {
					toRemove.add((Resource) specifications.next().getObject());
				}
				specifications.close();

			}
			nodes.close();
			toRemove.forEach(res -> {
				try {
					RepositoryResult<Statement> statements = conn.getStatements(res, null, null, false);
					conn.remove(statements);
					statements.close();
				} catch (RepositoryException e) {
					logger.error("Repository {} Error {}",repository, e.getMessage());
				}
			});
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Failure deletion : " + structure);
		}
	}
	

}
