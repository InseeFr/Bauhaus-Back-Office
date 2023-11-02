package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Getters only get on publication base
 * Setters (publish methods and execute methods) operate on publication and internal publication base
 * @author rco0ck
 *
 */
@org.springframework.stereotype.Repository("RepositoryPublication")
public class RepositoryPublication{
	private final String rdfServerPublicationExt;
	private final String idRepositoryPublicationExt;
	private final String rdfServerPublicationInt;
	private final String idRepositoryPublicationInt;
	private final RepositoryUtils repositoryUtils;
	
	private static final String THREE_PARAMS_LOG = "{} {} {}";

	private static final String CONNECTION_TO = "Connection to ";

	private static final String FAILED = " failed";

	static final Logger logger = LoggerFactory.getLogger(RepositoryPublication.class);
	
	public RepositoryPublication(@Value("${fr.insee.rmes.bauhaus.sesame.publication.sesameServer}") String rdfServerPublicationExt, 	@Value("${fr.insee.rmes.bauhaus.sesame.publication.repository}") String idRepositoryPublicationExt,
								 @Value("${fr.insee.rmes.bauhaus.sesame.publication.interne.repository}") String rdfServerPublicationInt, @Value("${fr.insee.rmes.bauhaus.sesame.publication.interne.repository}") String idRepositoryPublicationInt,
								 @Autowired RepositoryUtils repositoryUtils	) {
		this.rdfServerPublicationExt=rdfServerPublicationExt;
		this.idRepositoryPublicationExt=idRepositoryPublicationExt;
		this.repositoryUtils=repositoryUtils;
		this.rdfServerPublicationInt=rdfServerPublicationInt;
		this.idRepositoryPublicationInt=idRepositoryPublicationInt;
		 repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt);
		 repositoryUtils.initRepository(rdfServerPublicationInt,idRepositoryPublicationInt);
	}

	
	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public String getResponse(String query) throws RmesException {
		return RepositoryUtils.getResponse(query, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}
	
	/**
	 * Method which aims to produce response from a sparql query for internal Repository
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 */
	public String getResponseInternalPublication(String query) throws RmesException {
		return RepositoryUtils.getResponse(query, repositoryUtils.initRepository(rdfServerPublicationInt, idRepositoryPublicationInt));
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONArray
	 * @throws RmesException 
	 */
	public JSONArray getResponseAsArray(String query) throws RmesException {
		return RepositoryUtils.getResponseAsArray(query, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}

	/**
	 * Method which aims to produce response from a sparql query
	 * 
	 * @param query
	 * @return JSONObject
	 * @throws RmesException 
	 */
	public JSONObject getResponseAsObject(String query) throws RmesException {
		return RepositoryUtils.getResponseAsObject(query, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}

	/**
	 * Method which aims to produce response from a sparql ASK query
	 * 
	 * @param query
	 * @return String
	 * @throws RmesException 
	 * @throws JSONException 
	 */
	public Boolean getResponseAsBoolean(String query) throws  RmesException {
		return RepositoryUtils.getResponseForAskQuery(query, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}
	
	/**
	 * Method which aims to execute sparql update
	 * 
	 * @param updateQuery
	 * @return String
	 * @throws RmesException 
	 */
	public HttpStatus executeUpdate(String updateQuery) throws RmesException {
		HttpStatus status = RepositoryUtils.executeUpdate(updateQuery, repositoryUtils.initRepository(rdfServerPublicationInt, idRepositoryPublicationInt));
		if (status.is2xxSuccessful() ) {
			status = RepositoryUtils.executeUpdate(updateQuery, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
		}
		return status;
	}

	public void publishConcept(Resource concept, Model model, List<Resource> noteToClear,
			List<Resource> topConceptOfToDelete) throws RmesException {
		publishConcept(concept, model, noteToClear,topConceptOfToDelete, repositoryUtils.initRepository(rdfServerPublicationInt, idRepositoryPublicationInt));
		publishConcept(concept, model, noteToClear,topConceptOfToDelete, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}
	
	private void publishConcept(Resource concept, Model model, List<Resource> noteToClear,
			List<Resource> topConceptOfToDelete, Repository repo) throws RmesException {


		if (repo == null) {return ;}

		try {
			RepositoryConnection conn = repo.getConnection();
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
			logger.info("Publication of concept : {}", concept);
		} catch (RepositoryException e) {
			logger.error("Publication of concept : {} {} {}", concept, FAILED,  e.getMessage());
			logger.error(THREE_PARAMS_LOG, CONNECTION_TO , repo, FAILED);
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + repo + FAILED);
		}
	}

	public void publishResource(Resource resource, Model model, String type) throws RmesException {
		publishResource(resource, model, type, repositoryUtils.initRepository(rdfServerPublicationInt, idRepositoryPublicationInt));
		publishResource(resource, model, type, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}

	private static void publishResource(Resource resource, Model model, String type, Repository repo) throws RmesException {
		if (repo == null) {return ;}

		try {
			RepositoryConnection conn = repo.getConnection();
			conn.remove(resource, null, null);
			conn.add(model);
			conn.close();
			logger.info("Publication of Resource {} : {}" ,type, resource);
		} catch (RepositoryException e) {
			logger.error("Publication of Resource {} : {} {}" ,type, resource, FAILED);
			logger.error(THREE_PARAMS_LOG, CONNECTION_TO, repo, FAILED);
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + repo + FAILED);
		}
	}
	
	public void publishContext(Resource graph, Model model, String type) throws RmesException {
		publishContext(graph, model, type, repositoryUtils.initRepository(rdfServerPublicationInt, idRepositoryPublicationInt));
		publishContext(graph, model, type, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}
	
	public HttpStatus persistFile(InputStream input, RDFFormat format, String graph) throws RmesException {
		return RepositoryUtils.persistFile(input, format, graph, repositoryUtils.initRepository(rdfServerPublicationInt, idRepositoryPublicationInt), repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}
	

	public File getGraphAsFile(String context) throws RmesException {
			if (context != null) return RepositoryUtils.getCompleteGraphInTrig(repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt), context);
			return RepositoryUtils.getAllGraphsInZip(repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}
	
	public String[] getAllGraphs() throws RmesException {
		return RepositoryUtils.getAllGraphs(repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
	}
	
	private static void publishContext(Resource context, Model model, String type, Repository repo) throws RmesException {
		if (repo == null) {return ;}

		try {
			RepositoryConnection conn = repo.getConnection();
			conn.clear(context);
			conn.add(model);
			conn.close();
			logger.info("Publication of Graph {} : {}" ,type, context);
		} catch (RepositoryException e) {
			logger.error("Publication of Graph {} : {} {}" ,type, context, FAILED);
			logger.error(THREE_PARAMS_LOG, CONNECTION_TO, repo, FAILED);
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), CONNECTION_TO + repo + FAILED);
		}
	}

	public void clearStructureAndComponentForAllRepositories(Resource structure) throws RmesException {
		RepositoryUtils.clearStructureAndComponents(structure, repositoryUtils.initRepository(rdfServerPublicationExt, idRepositoryPublicationExt));
		RepositoryUtils.clearStructureAndComponents(structure, repositoryUtils.initRepository(rdfServerPublicationInt, idRepositoryPublicationInt));
	}

	private void clearConceptLinks(Resource concept, RepositoryConnection conn) throws RmesException {
		List<IRI> typeOfLink = Arrays.asList(SKOS.BROADER, SKOS.NARROWER, SKOS.MEMBER, DCTERMS.REFERENCES,
				DCTERMS.REPLACES, SKOS.RELATED, DCTERMS.IS_REPLACED_BY);

		for (IRI predicat : typeOfLink) {
			RepositoryResult<Statement> statements = null;
			try {
				statements = conn.getStatements(null, predicat, concept, false);
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
			try {
				conn.remove(statements);
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		}
	}
}
