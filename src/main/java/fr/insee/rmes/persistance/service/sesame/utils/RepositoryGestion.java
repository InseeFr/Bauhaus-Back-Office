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
import fr.insee.rmes.persistance.service.sesame.ontologies.EVOC;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;

public class RepositoryGestion {

	final static Logger logger = LogManager.getLogger(RepositoryGestion.class);

	public final static Repository REPOSITORY_GESTION = RepositoryUtils.initRepository(Config.SESAME_SERVER_GESTION,
			Config.REPOSITORY_ID_GESTION);

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public static String getResponse(String query) throws RmesException {
		return RepositoryUtils.getResponse(query, REPOSITORY_GESTION);
	}

	public static JSONArray getResponseAsArray(String query) throws RmesException {
		return RepositoryUtils.getResponseAsArray(query, REPOSITORY_GESTION);
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONObject
	 * @throws RmesException 
	 */
	public static JSONObject getResponseAsObject(String query) throws RmesException {
		return RepositoryUtils.getResponseAsObject(query, REPOSITORY_GESTION);
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
		return RepositoryUtils.getResponseAsBoolean(query, REPOSITORY_GESTION);
	}

	public static RepositoryResult<Statement> getStatements(RepositoryConnection con, Resource subject)
			throws RmesException {
		RepositoryResult<Statement> statements = null;
		try {
			statements = con.getStatements(subject, null, null, false);
		} catch (RepositoryException e) {
			throwsRmesException(e, "Failure get statements : " + subject);
		}
		return statements;
	}

	public static void closeStatements(RepositoryResult<Statement> statements) throws RmesException {
		try {
			statements.close();
		} catch (RepositoryException e) {
			throwsRmesException(e, "Failure close statements : ");
		}
	}

	
	public static void loadConcept(URI concept, Model model, List<List<URI>> notesToDeleteAndUpdate)
			throws RmesException {
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

			loadSimpleObject(concept, model, conn);
		} catch (OpenRDFException e) {
			throwsRmesException(e, "Failure load concept : " + concept);

		}
	}
	
	/**
	 * @param object
	 * @param model
	 * @param conn : can be null - initialized by the method
	 * @throws RmesException
	 */
	public static void loadSimpleObject(URI object, Model model, RepositoryConnection conn) throws RmesException {
		try {
			if (conn == null) {
				conn = REPOSITORY_GESTION.getConnection();
			}
			conn.remove(object, null, null);
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Failure load object : " + object);
			logger.error(e.getMessage());
			throw new RmesException(500, e.getMessage(), "Failure load object : " + object);

		}
	}

	public static void loadObjectWithReplaceLinks(URI object, Model model) throws RmesException {
		try {
			RepositoryConnection conn = REPOSITORY_GESTION.getConnection();
			clearReplaceLinks(object, conn);
			loadSimpleObject(object, model, conn);
		} catch (OpenRDFException e) {
			throwsRmesException(e, "Failure load object : " + object);
		}
	}

	public static void objectsValidation(List<URI> itemToValidateList, Model model) throws RmesException {
		try {
			RepositoryConnection conn = RepositoryGestion.REPOSITORY_GESTION.getConnection();
			for (URI item : itemToValidateList) {
				conn.remove(item, INSEE.IS_VALIDATED, null);
			}
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			throwsRmesException(e, "Failure validation : " + itemToValidateList);
		}
	}

	public static void clearConceptLinks(Resource concept, RepositoryConnection conn) throws RmesException {
		List<URI> typeOfLink = Arrays.asList(SKOS.BROADER, SKOS.NARROWER);
		getStatementsAndRemove(concept, conn, typeOfLink);
	}

	public static void clearReplaceLinks(Resource object, RepositoryConnection conn) throws RmesException {
		List<URI> typeOfLink = Arrays.asList(DCTERMS.REPLACES, DCTERMS.IS_REPLACED_BY);
		getStatementsAndRemove(object, conn, typeOfLink);
	}

	private static void getStatementsAndRemove(Resource object, RepositoryConnection conn, List<URI> typeOfLink)
			throws RmesException {
		for (URI predicat : typeOfLink) {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, object, false);
			} catch (RepositoryException e) {
				throwsRmesException(e, "Failure get "+predicat +" links from " + object);
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				throwsRmesException(e, "Failure remove "+predicat +" links from " + object);

			}
		}
	}

	public static void keepHierarchicalOperationLinks(Resource object, Model model) throws RmesException {
		List<URI> typeOfLink = Arrays.asList(DCTERMS.HAS_PART, DCTERMS.IS_PART_OF);
		RepositoryConnection conn = null;
		try {
			conn = RepositoryGestion.REPOSITORY_GESTION.getConnection();
			getHierarchicalOperationLinksModel(object, model, typeOfLink, conn);
		} catch (RmesException e) {
			throw e;
		} catch (Exception e) {
			throwsRmesException(e, "Failure keepHierarchicalOperationLinks : "+ object);
		}

	}
	

	private static void getHierarchicalOperationLinksModel(Resource object, Model model, List<URI> typeOfLink,
			RepositoryConnection conn) throws RmesException {
		for (URI predicat : typeOfLink) {
			RepositoryResult<Statement> statements;
			try {
				statements = conn.getStatements(null, predicat, object, false);
				addStatementToModel(model, statements);
				conn.remove(statements);

				statements = conn.getStatements(object, predicat, null, false);
				addStatementToModel(model, statements);
				conn.remove(statements);
			} catch (RepositoryException e) {
				throwsRmesException(e, "Failure getHierarchicalOperationLinksModel : "+ object);
			}
		}

	}

	private static void addStatementToModel(Model model, RepositoryResult<Statement> statements)
			throws RepositoryException {
		while (statements.hasNext()) {
			Statement st = statements.next();
			model.add(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
		}
	}
	
	private static void throwsRmesException(Exception e, String details) throws RmesException {
		logger.error(details);
		logger.error(e.getMessage());
		throw new RmesException(500, e.getMessage(), details);
	}




}
