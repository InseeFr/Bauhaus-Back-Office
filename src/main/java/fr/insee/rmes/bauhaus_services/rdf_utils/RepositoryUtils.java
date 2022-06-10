package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
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
import org.springframework.http.HttpStatus;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

public abstract class RepositoryUtils {
	
	private static final String BINDINGS = "bindings";
	private static final String RESULTS = "results";
	private static final String EXECUTE_QUERY_FAILED = "Execute query failed : ";

	
	static final Logger logger = LogManager.getLogger(RepositoryUtils.class);


	public static Repository initRepository(String sesameServer, String repositoryID) {
		if (sesameServer==null||sesameServer.equals("")) {return null;}
		Repository repo = new HTTPRepository(sesameServer, repositoryID);
		try {
			repo.init();
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
		Update update = null;
		String queryWithPrefixes = QueryUtils.PREFIXES + updateQuery;
		try {
			RepositoryConnection conn = repository.getConnection();
			update = conn.prepareUpdate(QueryLanguage.SPARQL, queryWithPrefixes);
			update.execute();
			conn.close();
		} catch (RepositoryException e) {
			logger.error("{} {} {}",EXECUTE_QUERY_FAILED, updateQuery, repository);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), EXECUTE_QUERY_FAILED + updateQuery);		
		}
		return(HttpStatus.OK);
	}
	
	/**
	 * Method which aims to load a file in database
	 * @param graph 
	 * @param secondRepo TODO
	 * @param updateQuery
	 * @return String
	 * @throws RmesException 
	 */
	public static HttpStatus persistFile(InputStream input,RDFFormat format, String graph, Repository repository, Repository secondRepo) throws RmesException {
		if (repository == null) {return HttpStatus.EXPECTATION_FAILED;}
		try {
			// add the RDF data from the inputstream directly to our database if ttl
			if (RDFFormat.TRIG != format) {
				RepositoryConnection conn = repository.getConnection();
				conn.add(input, format, RdfUtils.toURI(graph));
				conn.close();
				if (secondRepo != null ) {
					RepositoryConnection conn2 = secondRepo.getConnection();
					conn2.add(input, format, RdfUtils.toURI(graph));
					conn2.close();
				}
				logger.info("Graph loaded {}",graph);
			}else { //trig must be parsed before
				Model model = new LinkedHashModel();
				TriGParser rdfParser = new TriGParser();
				rdfParser.setRDFHandler(new StatementCollector(model));
				rdfParser.parse(input, "");
				clearGraphs(model.contexts(), repository);
				RepositoryConnection conn = repository.getConnection();
				conn.add(model);
				conn.close();
				if (secondRepo != null ) {
					clearGraphs(model.contexts(), secondRepo);
					RepositoryConnection conn2 = secondRepo.getConnection();
					conn2.add(model);
					conn2.close();
				}
				logger.info("Graphs loaded {}",model.contexts());
			}

		} catch (RepositoryException |RDFParseException |IOException e) {
			logger.error("{} {} {}","Persist file failed", format, repository);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Persist file failed - "+e.getClass());		
		}
		return(HttpStatus.OK);
	}
	
	private static void clearGraphs(Set<Resource> graphs, Repository repository) throws RmesException {
		try {
			RepositoryConnection conn = repository.getConnection();
			graphs.forEach(conn::clear);
			conn.close();
		} catch (RepositoryException e) {
			logger.error("Failed to clear graphs {}", graphs);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Failed to clear graphs" + graphs);
		}
	}
	
	public RepositoryResult<Statement> getCompleteGraph(RepositoryConnection con, Resource context) throws RmesException {
		RepositoryResult<Statement> statements = null;
		try {
			statements = con.getStatements(null, null, null,context); //get the complete Graph
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Failure get following graph : " + context);
		}
		return statements;
	}
	
	public static File getCompleteGraphInTrig( Repository repository, String context) throws RmesException {
		RepositoryConnection connection = repository.getConnection();
		Resource graphToExport =  RdfUtils.toURI(context);
		String filename = context.replace(RdfUtils.getBaseGraph(),"").replace("/","_").concat(".trig");
		File tempFile = null;
		try {
			tempFile = File.createTempFile(filename, ".trig");
		} catch (IOException e1) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e1.getMessage(), "IOException - Failed to create temp file");
		}
		try(OutputStream out = new FileOutputStream(tempFile)){		
			RDFHandler writer =  new TriGWriter(out);
			connection.export(writer, graphToExport) ;//here if graphToExport is null => return statement without graph
			connection.close();
			return tempFile;
		} catch (IOException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "IOException - Failed to getGraph in file");
		}
	}
	
	private static File getCompleteGraphInTrigWithoutException(Repository repo, String context) {
		try {
			logger.debug("Begin to get trig file for {}", context);
			return getCompleteGraphInTrig(repo, context);
		}catch(RmesException | RepositoryException e) {
			logger.error("Graph {} can't be writen in a trig - {}, {}", context, e.getClass(), e.getMessage());
		}
		return null;
	}
	
	public static File getAllGraphsInZip(Repository repo) throws RmesException {
		//Get all graphs name
		String[] graphs = getAllGraphs(repo);
		if (graphs == null) throw new RmesException(HttpStatus.EXPECTATION_FAILED,"Can't find any graph","Check database");
		
		//For each graph, create a trig file with statements
		final var fileCounter = new AtomicInteger();
		Stream<File> files = Arrays.stream(graphs).map(graph -> getCompleteGraphInTrigWithoutException(repo, graph)).filter(Objects::nonNull);

		//Compile all trig in a zip
		File tempZipFile = null;
		try {
			tempZipFile = File.createTempFile("exportAll", ".zip");
		} catch (IOException e1) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e1.getMessage(), "IOException - Failed to create temp file");
		}
		
		try(OutputStream outZip = new FileOutputStream(tempZipFile)){
			 ZipOutputStream zos = new ZipOutputStream(outZip);
			 files.forEach(file -> {
				fileCounter.incrementAndGet();
				addFileToZip(zos, file);
			 });
	        zos.close();
			if (graphs.length != fileCounter.get()) throw new RmesException(HttpStatus.EXPECTATION_FAILED,"Some graphs can't be writen in a trig","Error in processing getCompleteGraphInTrig");
			return tempZipFile;
		} catch (IOException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "IOException - Failed to getGraph in file");
		}
		
    }

	public static void addFileToZip(ZipOutputStream zos, File file) {
		String filename = file.getName();
		logger.debug("Add file {} to zip", filename);
		ZipEntry entry = new ZipEntry(filename);
		    try {
				zos.putNextEntry(entry);
			    if (file.isFile()) {
			        copyBytes(zos, new FileInputStream(file));
			    }
			    zos.closeEntry();
			} catch (IOException e) {
				logger.error("IOException - Can't add file {} to zip", filename);
			}

	}
		
		
		
	private static void copyBytes(ZipOutputStream zipOut, FileInputStream fileInputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int length;
        while((length = fileInputStream.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
	}

	public static String[] getAllGraphs(Repository repo) throws RmesException {
		RepositoryConnection connection = repo.getConnection();
		TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,GenericQueries.getAllGraphs());//.evaluate(writer);
		TupleQueryResult rs = tupleQuery.evaluate();
		String[] graphs = rs.stream().map(g -> g.getValue("g").stringValue()).toArray(String[]::new);
		logger.info("Graphs in database : {}", (graphs != null ? graphs.length : "0"));
		connection.close();
		return graphs;
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
		} catch (RepositoryException e) {
			logAndThrowError(query, e);		
		}
		return stream.toString();
	}
	
	/**
	 * Method which aims to execute a sparql ASK query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static boolean executeAskQuery(RepositoryConnection conn, String query) throws RmesException {
		BooleanQuery tupleQuery = null;
		try {
			tupleQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query);
			return tupleQuery.evaluate();
		} catch (RepositoryException e) {
			logAndThrowError(query, e);		
		}
		return false;
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
		} catch (RepositoryException e) {
			logAndThrowError(query, e);		
		}
		return response;
	}

	private static void logAndThrowError(String query, RepositoryException e) throws RmesException {
		logger.error("{} {}",EXECUTE_QUERY_FAILED, query);
		logger.error(e.getMessage());
		throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), EXECUTE_QUERY_FAILED + query);
	}
	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static boolean getResponseForAskQuery(String query, Repository repository) throws RmesException {
		boolean response = false;
		try {
			RepositoryConnection conn = repository.getConnection();
			String queryWithPrefixes = QueryUtils.PREFIXES + query;
			response = executeAskQuery(conn, queryWithPrefixes);
			conn.close();
		} catch (RepositoryException e) {
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
	 * Return a JsonArray containing a list of jsonobject (key value)
	 * @param jsonSparql
	 * @return
	 */
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
			set.forEach(s -> jsonResults.put(s, ((JSONObject) json.get(s)).get(Constants.VALUE)));
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
		if (jsonSparql.get(RESULTS) == null) {
			return null;
		}

		int nbRes = ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS)).length();

		for (int i = 0; i < nbRes; i++) {
			final JSONObject json = (JSONObject) ((JSONArray) ((JSONObject) jsonSparql.get(RESULTS)).get(BINDINGS))
					.get(i);
			Set<String> set = json.keySet();
			set.forEach(s -> arrayRes.put(((JSONObject)json.get(s)).get(Constants.VALUE)));
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
		set.forEach(s -> jsonResults.put(s, ((JSONObject) json.get(s)).get(Constants.VALUE)));
		return jsonResults;
	}
	
	public static void clearStructureAndComponents(Resource structure, Repository repository) throws RmesException {
		List<Resource> toRemove = new ArrayList<>();
		try (RepositoryConnection conn = repository.getConnection()){
			RepositoryResult<Statement> nodes = null;
			RepositoryResult<Statement> specifications = null;
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
