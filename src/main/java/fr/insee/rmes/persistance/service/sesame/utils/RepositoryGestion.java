package fr.insee.rmes.persistance.service.sesame.utils;

import java.util.Arrays;
import java.util.List;

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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.sesame.ontologies.EVOC;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;

public class RepositoryGestion {

	final static Logger logger = LogManager.getLogger(RepositoryGestion.class);

	public final static Repository REPOSITORY_GESTION = RepositoryUtils.initRepository(Config.SESAME_SERVER_GESTION, Config.REPOSITORY_ID_GESTION);




	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 */
	public static String getResponse(String query) {
		return RepositoryUtils.getResponse(query,REPOSITORY_GESTION);
	}

	public static JSONArray getResponseAsArray(String query) {
		return RepositoryUtils.getResponseAsArray(query, REPOSITORY_GESTION);
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONObject
	 */
	public static JSONObject getResponseAsObject(String query) {
		return RepositoryUtils.getResponseAsObject(query,REPOSITORY_GESTION);
	}

	/**
	 * Method which aims to produce response from a sparql ASK query
	 * 
	 * @param query
	 * @return String
	 */
	public static Boolean getResponseAsBoolean(String query) {
		return RepositoryUtils.getResponseAsBoolean(query, REPOSITORY_GESTION);
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

	public static void loadSimpleObject(URI object, Model model) {
		try {
			RepositoryConnection conn = REPOSITORY_GESTION.getConnection();
			conn.remove(object, null, null);
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Failure load : " + object);
			logger.error(e.getMessage());
		}
	}

	
	public static void loadObjectWithReplaceLinks(URI object, Model model) {
		try {
			RepositoryConnection conn = REPOSITORY_GESTION.getConnection();
			clearReplaceLinks(object, conn);
			conn.remove(object, null, null);
			conn.add(model);
			conn.close();
		} catch (OpenRDFException e) {
			logger.error("Failure load object : " + object);
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
	
	public static void clearReplaceLinks(Resource object, RepositoryConnection conn) {
		List<URI> typeOfLink = Arrays.asList(DCTERMS.REPLACES, DCTERMS.IS_REPLACED_BY);

		typeOfLink.forEach(predicat -> {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, object, false);
			} catch (RepositoryException e) {
				logger.error("Failure clearReplaceLinks : " + object);
				logger.error(e.getMessage());
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				logger.error("Failure clearReplaceLinks close statement : ");
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
					addStatementToModel(model, statements);
					conn.remove(statements);
					
					statements = conn.getStatements(object, predicat, null, false);
					addStatementToModel(model, statements);
					conn.remove(statements);
				} catch (RepositoryException e) {
					logger.error("Failure getHierarchicalOperationLinksModel : " + object);
					logger.error(e.getMessage());
				}			
		});
	}

	private static void addStatementToModel(Model model, RepositoryResult<Statement> statements)
			throws RepositoryException {
		while (statements.hasNext()) {
			Statement st = statements.next();
			model.add(st.getSubject(), st.getPredicate(),st.getObject(), st.getContext());
		}
	}

}
