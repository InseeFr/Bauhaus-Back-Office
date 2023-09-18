package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.EVOC;
import fr.insee.rmes.persistance.ontologies.INSEE;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component("RepositoryGestion")
@DependsOn("AppContext")
public class RepositoryGestion  {

	@Autowired
	Config config;

	@Autowired
	private RepositoryUtils repositoryUtils;

	private static final String FAILURE_LOAD_OBJECT = "Failure load object : ";
	private static final String FAILURE_REPLACE_GRAPH = "Failure replace graph : ";
	private static final String FAILURE_DELETE_OBJECT = "Failure delete object";

	static final Logger logger = LoggerFactory.getLogger(RepositoryGestion.class);

	@PostConstruct
	public void init() {
		repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion());
	}


	/**
	 * Method which aims to produce response from a sparql query
	 *
	 * @param query
	 * @return String
	 * @throws RmesException
	 */
	public String getResponse(String query) throws RmesException {
		return repositoryUtils.getResponse(query, repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()));
	}

	/**
	 * Method which aims to execute sparql update
	 *
	 * @param updateQuery
	 * @return String
	 * @throws RmesException
	 */
	public HttpStatus executeUpdate(String updateQuery) throws RmesException {
		return repositoryUtils.executeUpdate(updateQuery, repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()));
	}

	/**
	 * Method which aims to produce response from a sparql query
	 *
	 * @param query
	 * @return JSONObject
	 * @throws RmesException
	 */
	public JSONObject getResponseAsObject(String query) throws RmesException {
			return repositoryUtils.getResponseAsObject(query, repositoryUtils.initRepository(config.getRdfServerGestion(),
					config.getRepositoryIdGestion()));
	}

	public JSONArray getResponseAsArray(String query) throws RmesException {
		return repositoryUtils.getResponseAsArray(query, repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()));
	}

	public JSONArray getResponseAsJSONList(String query) throws RmesException {
		return repositoryUtils.getResponseAsJSONList(query, repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()));
	}

	/**
	 * Method which aims to produce response from a sparql ASK query
	 *
	 * @param query
	 * @return String
	 * @throws RmesException
	 * @throws JSONException
	 */
	public boolean getResponseAsBoolean(String query) throws RmesException {
		return repositoryUtils.getResponseForAskQuery(query, repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()));
	}

	public RepositoryResult<Statement> getStatements(RepositoryConnection con, Resource subject)
			throws RmesException {
		RepositoryResult<Statement> statements = null;
		if (con == null) {
			con = repositoryUtils.initRepository(config.getRdfServerGestion(),
					config.getRepositoryIdGestion()).getConnection();
		}
		try {
			statements = con.getStatements(subject, null, null, false);
		} catch (RepositoryException e) {
			throwsRmesException(e, "Failure get statements : " + subject);
		}
		return statements;
	}

	public RepositoryResult<Statement> getHasPartStatements(RepositoryConnection con, Resource object)
			throws RmesException {
		return getStatementsPredicatObject(con, DCTERMS.HAS_PART,object);
	}

	public RepositoryResult<Statement> getReplacesStatements(RepositoryConnection con, Resource object)
			throws RmesException {
		return getStatementsPredicatObject(con, DCTERMS.REPLACES,object);
	}

	public RepositoryResult<Statement> getIsReplacedByStatements(RepositoryConnection con, Resource object)
			throws RmesException {
		return getStatementsPredicatObject(con, DCTERMS.IS_REPLACED_BY,object);
	}


	private RepositoryResult<Statement> getStatementsPredicatObject(RepositoryConnection con, IRI predicate, Resource object)
			throws RmesException {

			if (con == null) {
				con = repositoryUtils.initRepository(config.getRdfServerGestion(),
						config.getRepositoryIdGestion()).getConnection();
			}

		RepositoryResult<Statement> statements = null;
		try {
			statements = con.getStatements(null, predicate, object, false);
		} catch (RepositoryException e) {
			throwsRmesException(e, "Failure get " +RdfUtils.toString(predicate) + " statements : " + object);
		}
		return statements;
	}

	public File getGraphAsFile(String context) throws RmesException {
			if (context != null) return repositoryUtils.getCompleteGraphInTrig(repositoryUtils.initRepository(config.getRdfServerGestion(),
					config.getRepositoryIdGestion()), context);
			return repositoryUtils.getAllGraphsInZip(repositoryUtils.initRepository(config.getRdfServerGestion(),
					config.getRepositoryIdGestion()));
	}

	public String[] getAllGraphs() throws RmesException {
		return repositoryUtils.getAllGraphs(repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()));
	}

	public void closeStatements(RepositoryResult<Statement> statements) throws RmesException {
		try {
			statements.close();
		} catch (RepositoryException e) {
			throwsRmesException(e, "Failure close statements : ");
		}
	}

	public void loadConcept(IRI concept, Model model, List<List<IRI>> notesToDeleteAndUpdate)
			throws RmesException {
		try (RepositoryConnection conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()).getConnection() ){
			// notes to delete
			for (IRI note : notesToDeleteAndUpdate.get(0)) {
				conn.remove(note, null, null);
			}
			// notes to update
			for (IRI note : notesToDeleteAndUpdate.get(1)) {
				conn.remove(note, EVOC.NOTE_LITERAL, null);
			}
			// links to delete
			clearConceptLinks(concept);

			loadSimpleObject(concept, model, conn);
		} catch (RepositoryException e) {
			throwsRmesException(e, "Failure load concept : " + concept);

		}
	}

	public void deleteTripletByPredicateAndValue(Resource object, IRI predicate, Resource graph, RepositoryConnection conn, Value value) throws RmesException {
		try {
			if (conn == null) {
				conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
						config.getRepositoryIdGestion()).getConnection();
			}
			conn.remove(object, predicate, value, graph);
			conn.close();
		} catch (RepositoryException e) {
			logger.error(FAILURE_LOAD_OBJECT , object);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), FAILURE_LOAD_OBJECT + object);

		}
	}

	public void deleteTripletByPredicate(Resource object, IRI predicate, Resource graph, RepositoryConnection conn) throws RmesException {
		deleteTripletByPredicateAndValue(object, predicate, graph, conn, null);
	}

	public void loadSimpleObjectWithoutDeletion(IRI object, Model model, RepositoryConnection conn) throws RmesException {
		try {
			if (conn == null) {
				conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
						config.getRepositoryIdGestion()).getConnection();
			}
			conn.add(model);
			conn.close();
		} catch (RepositoryException e) {
			logger.error(FAILURE_LOAD_OBJECT , object);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), FAILURE_LOAD_OBJECT + object);

		}
	}

	/**
	 * @param object
	 * @param model
	 * @param conn : can be null - initialized by the method
	 * @throws RmesException
	 */
	public void loadSimpleObject(IRI object, Model model, RepositoryConnection conn) throws RmesException {

		try {
			if (conn == null) {
				conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
						config.getRepositoryIdGestion()).getConnection();
			}
			conn.remove(object, null, null);
			conn.add(model);
			conn.close();
		} catch (RepositoryException e) {
			logger.error(FAILURE_LOAD_OBJECT , object);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), FAILURE_LOAD_OBJECT + object);

		}
	}

	public void deleteObject(IRI object, RepositoryConnection conn) throws RmesException {

		try {
			if (conn == null) {
				conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
						config.getRepositoryIdGestion()).getConnection();
			}
			conn.remove(object, null, null);
			conn.close();
		} catch (RepositoryException e) {
			logger.error(FAILURE_DELETE_OBJECT , object);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), FAILURE_DELETE_OBJECT + object);

		}
	}

	/**
	 * @param graph
	 * @param model
	 * @param conn : can be null - initialized by the method
	 * @throws RmesException
	 */
	public void replaceGraph(Resource graph, Model model, RepositoryConnection conn) throws RmesException {

		try {
			if (conn == null) {
				conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
						config.getRepositoryIdGestion()).getConnection();
			}
			conn.clear(graph);
			conn.add(model);
			conn.close();
		} catch (RepositoryException e) {
			logger.error(FAILURE_REPLACE_GRAPH, graph);
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), FAILURE_REPLACE_GRAPH + graph);

		}
	}

	public HttpStatus persistFile(InputStream input, RDFFormat format, String graph) throws RmesException {
		return repositoryUtils.persistFile(input, format, graph, repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()), null);
	}

	public void loadObjectWithReplaceLinks(IRI object, Model model) throws RmesException {


		try (RepositoryConnection conn=repositoryUtils.initRepository(config.getRdfServerGestion(),
						config.getRepositoryIdGestion()).getConnection();)
		{
			clearReplaceLinks(object, conn);
			loadSimpleObject(object, model, conn);
		} catch (RepositoryException e) {
			throwsRmesException(e, FAILURE_LOAD_OBJECT + object);
		}
	}

	public void objectsValidation(List<IRI> collectionsToValidateList, Model model) throws RmesException {

		try {
			RepositoryConnection conn=repositoryUtils.initRepository(config.getRdfServerGestion(),
					config.getRepositoryIdGestion()).getConnection();
			for (IRI item : collectionsToValidateList) {
				conn.remove(item, INSEE.VALIDATION_STATE, null);
				conn.remove(item, INSEE.IS_VALIDATED, null);
			}
			conn.add(model);
			conn.close();
		} catch (RepositoryException e) {
			throwsRmesException(e, "Failure validation : " + collectionsToValidateList);
		}
	}

	public void objectValidation(IRI ressourceURI, Model model) throws RmesException {

		try {
			RepositoryConnection conn=repositoryUtils.initRepository(config.getRdfServerGestion(),
					config.getRepositoryIdGestion()).getConnection();
			conn.remove(ressourceURI, INSEE.VALIDATION_STATE, null);
			conn.remove(ressourceURI, INSEE.IS_VALIDATED, null);
			conn.add(model);
			conn.close();
		} catch (RepositoryException e) {
			throwsRmesException(e, "Failure validation : " + ressourceURI);
		}
	}

	public void clearConceptLinks(Resource concept) throws RmesException {
		RepositoryConnection conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()).getConnection();
		List<IRI> typeOfLink = Arrays.asList(SKOS.BROADER, SKOS.NARROWER, SKOS.RELATED, DCTERMS.IS_REPLACED_BY);
		getStatementsAndRemove(concept, conn, typeOfLink);
	}

	public void clearReplaceLinks(Resource object, RepositoryConnection conn) throws RmesException {
		conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()).getConnection();
		List<IRI> typeOfLink = Arrays.asList(DCTERMS.REPLACES, DCTERMS.IS_REPLACED_BY);
		getStatementsAndRemove(object, conn, typeOfLink);
	}

	private  void getStatementsAndRemove(Resource object, RepositoryConnection conn, List<IRI> typeOfLink)
			throws RmesException {
		conn = repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()).getConnection();
		for (IRI predicat : typeOfLink) {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, object, false);
			} catch (RepositoryException e) {
				throwsRmesException(e, "Failure get " + predicat + " links from " + object);
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				throwsRmesException(e, "Failure remove " + predicat + " links from " + object);

			}
		}
	}

	public void clearStructureNodeAndComponents(Resource structure) throws RmesException {
		repositoryUtils.clearStructureAndComponents(structure, repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()));
	}

	public void keepHierarchicalOperationLinks(Resource object, Model model) throws RmesException {
		List<IRI> typeOfLink = Arrays.asList(DCTERMS.HAS_PART, DCTERMS.IS_PART_OF);

		try (
				RepositoryConnection conn=repositoryUtils.initRepository(config.getRdfServerGestion(),
						config.getRepositoryIdGestion()).getConnection();
		){
			getHierarchicalOperationLinksModel(object, model, typeOfLink, conn);
		} catch (RmesException e) {
			throw e;
		} catch (Exception e) {
			throwsRmesException(e, "Failure keepHierarchicalOperationLinks : " + object);
		}

	}

	private void getHierarchicalOperationLinksModel(Resource object, Model model, List<IRI> typeOfLink,
			RepositoryConnection conn) throws RmesException {
		conn=repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()).getConnection();
		for (IRI predicat : typeOfLink) {
			RepositoryResult<Statement> statements;
			try {
				statements = conn.getStatements(null, predicat, object, false);
				addStatementToModel(model, statements);
				conn.remove(statements);

				statements = conn.getStatements(object, predicat, null, false);
				addStatementToModel(model, statements);
				conn.remove(statements);
			} catch (RepositoryException e) {
				throwsRmesException(e, "Failure getHierarchicalOperationLinksModel : " + object);
			}
		}

	}

	private void addStatementToModel(Model model, RepositoryResult<Statement> statements) {
		while (statements.hasNext()) {
			Statement st = statements.next();
			model.add(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
		}
	}

	private static void throwsRmesException(Exception e, String details) throws RmesException {
		logger.error(details);
		logger.error("{} {}" , e.getClass(), e.getMessage());
		throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(),  e.getClass() + " "+ e.getMessage(), details);
	}

	public void loadSimpleObject(IRI geoIRI, Model model) throws RmesException {
		loadSimpleObject(geoIRI, model, null);
	}

	public RepositoryConnection getConnection() throws RmesException {
		return repositoryUtils.getConnection(repositoryUtils.initRepository(config.getRdfServerGestion(),
				config.getRepositoryIdGestion()));
	}


	public void overrideTriplets(IRI simsUri, Model model, Resource graph) throws RmesException {


		try {
			RepositoryConnection connection =repositoryUtils.initRepository(config.getRdfServerGestion(),
					config.getRepositoryIdGestion()).getConnection();
			model.predicates().forEach(predicate -> connection.remove(simsUri, predicate, null, graph));
			connection.add(model);
			connection.close();
		} catch (RepositoryException e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), FAILURE_LOAD_OBJECT);
		}
	}

	public RepositoryResult<Statement> getCompleteGraph(RepositoryConnection con, Resource graphIri) throws RmesException {
		return repositoryUtils.getCompleteGraph(con,graphIri);
	}

	/**
	 * Method to clear an entire graph.
	 */
	public void clearGraph() throws IOException {

		String server = config.getRdfServerGestion();
		String repository = config.getRepositoryIdGestion();

		String serverProd = config.getRdfServerGestionProd();
		String repositoryProd = config.getRepositoryIdGestionProd();

		RepositoryConnection connection =
				repositoryUtils.initRepository(server, repository).getConnection();

		RepositoryConnection connectionProd =
				repositoryUtils.initRepository(serverProd, repositoryProd).getConnection();

		File file = new File("export.trig");
		FileOutputStream outputStream = new FileOutputStream(file);
		RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, outputStream);

		connectionProd.exportStatements(null, null, null, false, writer);
		connection.clear();
		IOUtils.close(outputStream);
		connection.add(file, RDFFormat.TRIG);
	}
}
