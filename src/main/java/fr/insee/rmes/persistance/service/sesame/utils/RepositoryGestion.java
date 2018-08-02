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
import fr.insee.rmes.persistance.service.sesame.ontologies.EVOC;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;

public class RepositoryGestion {

	final static Logger logger = LogManager.getLogger(RepositoryGestion.class);

	public final static Repository REPOSITORY_GESTION = initRepository(Config.SESAME_SERVER_GESTION, Config.REPOSITORY_ID_GESTION);


	public static Repository initRepository(String sesameServer, String repositoryID) {
		Repository repo = new HTTPRepository(sesameServer, repositoryID);
		try {
			repo.initialize();
		} catch (OpenRDFException e) {
			logger.error("Initialisation de la connection à la base sesame " + sesameServer + " impossible");
			logger.error(e.getMessage());
		}
		return repo;
	}

	public static RepositoryConnection getConnection(Repository repository) {
		RepositoryConnection con = null;
		try {
			con = repository.getConnection();
		} catch (OpenRDFException e) {
			logger.error("Connection au repository impossible : " + repository.getDataDir());
			logger.error(e.getMessage());
			e.getMessage();
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
		Repository repository = REPOSITORY_GESTION;
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
	public static JSONArray getResponseAsArray(String query) {
		String response = getResponse(query);
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
	public static JSONObject getResponseAsObject(String query) {
		JSONArray resArray = getResponseAsArray(query);
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
	public static Boolean getResponseAsBoolean(String query) {
		JSONObject res = new JSONObject(getResponse(query));
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

	public static RepositoryResult<Statement> getStatements(RepositoryConnection con, Resource subject) {
		RepositoryResult<Statement> statements = null;
		try {
			statements = con.getStatements(subject, null, null, false);
		} catch (RepositoryException e) {
			logger.error("Failure get statements : " + subject);
			logger.error(e.getMessage());
		}
		return statements;
	}

	public static void closeStatements(RepositoryResult<Statement> statements) {
		try {
			statements.close();
		} catch (RepositoryException e) {
			logger.error("Failure close statements : ");
			logger.error(e.getMessage());
		}
	}

	public static void loadConcept(URI concept, Model model, List<List<URI>> notesToDeleteAndUpdate) {
		try {
			RepositoryConnection conn = REPOSITORY_GESTION.getConnection();
			// notes to delete
			for (URI note : notesToDeleteAndUpdate.get(0)) {
				conn.remove(note, null, null);
			}
			// notes to update
			for (URI note : notesToDeleteAndUpdate.get(1)) {
				conn.remove(note, EVOC.NOTE_LITERAL, null);
			}
			// links to delete
			clearConceptLinks(concept, conn);

			conn.remove(concept, null, null);
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Failure load concept : " + concept);
			logger.error(e.getMessage());
		}
	}

	public static void loadCollection(URI collection, Model model) {
		try {
			RepositoryConnection conn = REPOSITORY_GESTION.getConnection();
			conn.remove(collection, null, null);
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Failure load collection : " + collection);
			logger.error(e.getMessage());
		}
	}

	public static void loadFamily(URI family, Model model) {
		try {
			RepositoryConnection conn = REPOSITORY_GESTION.getConnection();
			conn.remove(family, null, null);
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Failure load family : " + family);
			logger.error(e.getMessage());
		}
	}
	
	public static void loadSeries(URI series, Model model) {
		try {
			RepositoryConnection conn = REPOSITORY_GESTION.getConnection();
			clearSeriesLinks(series, conn);
			conn.remove(series, null, null);
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Failure load series : " + series);
			logger.error(e.getMessage());
		}
	}
	
	public static void objectsValidation(List<URI> itemToValidateList, Model model) {
		try {
			RepositoryConnection conn = RepositoryGestion.REPOSITORY_GESTION.getConnection();
			for (URI item : itemToValidateList) {
				conn.remove(item, INSEE.IS_VALIDATED, null);
			}
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Failure validation : " + itemToValidateList);
			logger.error(e.getMessage());
		}
	}

	public static void clearConceptLinks(Resource concept, RepositoryConnection conn) {
		List<URI> typeOfLink = Arrays.asList(SKOS.BROADER, SKOS.NARROWER);

		typeOfLink.forEach(predicat -> {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, concept, false);
			} catch (RepositoryException e) {
				logger.error("Failure clearConceptLinks : " + concept);
				logger.error(e.getMessage());
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				logger.error("Failure clearConceptLinks close statement : ");
				logger.error(e.getMessage());
			}
		});
	}
	
	public static void clearSeriesLinks(Resource series, RepositoryConnection conn) {
		List<URI> typeOfLink = Arrays.asList(DCTERMS.REPLACES, DCTERMS.IS_REPLACED_BY);

		typeOfLink.forEach(predicat -> {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, series, false);
			} catch (RepositoryException e) {
				logger.error("Failure clearSeriesLinks : " + series);
				logger.error(e.getMessage());
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				logger.error("Failure clearSeriesLinks close statement : ");
				logger.error(e.getMessage());
			}
		});
	}
	
	public static void keepHierarchicalOperationLinks(Resource object, Model model) {
		List<URI> typeOfLink = Arrays.asList(DCTERMS.HAS_PART, DCTERMS.IS_PART_OF);
		RepositoryConnection conn = null;
		try {
			conn = RepositoryGestion.REPOSITORY_GESTION.getConnection();
			getHierarchicalOperationLinksModel(object, model, typeOfLink, conn);
		} catch (Exception e) {
			logger.error("Failure keepHierarchicalOperationLinks : " + object);
			logger.error(e.getMessage());
		}
		
	}

	private static void getHierarchicalOperationLinksModel(Resource object, Model model, List<URI> typeOfLink,RepositoryConnection conn) {
		typeOfLink.forEach(predicat -> {
			RepositoryResult<Statement> statements;
				try {
					statements = conn.getStatements(null, predicat, object, false);
					while (statements.hasNext()) {
						Statement st = statements.next();
						model.add(st.getSubject(), st.getPredicate(),st.getObject(), st.getContext());
					}
					conn.remove(statements);
					
					statements = conn.getStatements(object, predicat, null, false);
					while (statements.hasNext()) {
						Statement st = statements.next();
						model.add(st.getSubject(), st.getPredicate(),st.getObject(), st.getContext());
					}
					conn.remove(statements);
				} catch (RepositoryException e) {
					logger.error("Failure getHierarchicalOperationLinksModel : " + object);
					logger.error(e.getMessage());
				}			
		});
	}

}
