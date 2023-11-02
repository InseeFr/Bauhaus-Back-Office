package fr.insee.rmes.bauhaus_services.concepts.publication;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.XKOS;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConceptsPublication extends RdfService{

	String[] notes = {"scopeNote","definition","editorialNote"} ;
	String[] links = {"inScheme","disseminationStatus","references",Constants.ISREPLACEDBY};
	String[] ignoredAttrs = {"isValidated","changeNote",Constants.CREATOR,Constants.CONTRIBUTOR};

	public void publishConcepts(JSONArray conceptsToPublish) throws RmesException {
		for (int i = 0; i < conceptsToPublish.length(); ++i) {
			String conceptId = conceptsToPublish.getString(i);
			Model model = new LinkedHashModel();
			List<Resource> noteToClear = new ArrayList<>();
			List<Resource> topConceptOfToDelete = new ArrayList<>();
			checkTopConceptOf(conceptId, model);
			Resource concept = RdfUtils.conceptIRI(conceptId);

			RepositoryConnection con = repoGestion.getConnection();
			RepositoryResult<Statement> statements = repoGestion.getStatements(con, concept);

			try {
				boolean hasBroader = false;
				while (statements.hasNext()) {
					Statement st = statements.next();
					
					// Notes, transform URI and get attributs
					hasBroader = prepareOneTripleToPublicationAndCheckIfHasBroader(model, noteToClear, topConceptOfToDelete, con, st, hasBroader);
				}
				if (!hasBroader) {
					model.add(publicationUtils.tranformBaseURIToPublish(concept), SKOS.TOP_CONCEPT_OF, publicationUtils.tranformBaseURIToPublish(RdfUtils.conceptScheme()),
							RdfUtils.conceptGraph());
				}
			} catch (RepositoryException e) {
				repoGestion.closeStatements(statements);
				con.close();
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			} 
			
			repoGestion.closeStatements(statements);
			publishMemberLinks(concept, model, con);
			con.close();
			
			Resource conceptToPublish = publicationUtils.tranformBaseURIToPublish(concept);
			repositoryPublication.publishConcept(conceptToPublish, model, noteToClear, topConceptOfToDelete);
		}

	}



	private Boolean prepareOneTripleToPublicationAndCheckIfHasBroader(Model model, List<Resource> noteToClear,
			List<Resource> topConceptOfToDelete, RepositoryConnection con, Statement st, boolean hasBroader)
			throws RmesException {
		
		Resource subject =  publicationUtils.tranformBaseURIToPublish(st.getSubject());
		Resource graph = st.getContext();
		String predicat = RdfUtils.toString(st.getPredicate());
		
		if (PublicationUtils.stringEndsWithItemFromList(predicat,notes)) {
			model.add(subject, st.getPredicate(), publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
					graph);
			publishExplanatoryNotes(con, RdfUtils.toURI(st.getObject().toString()), model);
			noteToClear.add(publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()));
		}
		// Other URI to transform	
		else if (PublicationUtils.stringEndsWithItemFromList(predicat,links)) {
			model.add(subject, st.getPredicate(), publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
					graph);
		}
		else if (predicat.endsWith("related")) {
			model.add(subject, st.getPredicate(), publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
					graph);
			model.add(publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), SKOS.RELATED, subject, graph);
		} else if (predicat.endsWith(Constants.REPLACES)) {
			model.add(subject, st.getPredicate(), publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
					graph);
			model.add(publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), DCTERMS.IS_REPLACED_BY, subject, graph);
		} else if (predicat.endsWith("broader")) {
			hasBroader = true;
			model.add(subject, st.getPredicate(), publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
					graph);
			model.add(publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), SKOS.NARROWER, subject, graph);
		}
		// Narrower links
		else if (predicat.endsWith("narrower")) {
			Resource object = publicationUtils.tranformBaseURIToPublish((Resource) st.getObject());
			topConceptOfToDelete.add(object);
			model.add(subject, st.getPredicate(), object, graph);
			model.add(object, SKOS.BROADER, subject, graph);
		} else if (PublicationUtils.stringEndsWithItemFromList(predicat,ignoredAttrs)) {
			// nothing, wouldn't copy this attr
		}
		// Literals
		else {
			model.add(subject, st.getPredicate(), st.getObject(), graph);
		}
		
		return hasBroader;
	}
	


	private void checkTopConceptOf(String conceptId, Model model)  throws RmesException {
		JSONArray conceptsToCheck = repositoryPublication.getResponseAsArray(ConceptsQueries.getNarrowers(conceptId));
		for (int i = 0; i < conceptsToCheck.length(); i++) {
			String id = conceptsToCheck.getJSONObject(i).getString("narrowerId");
			if (!repoGestion.getResponseAsBoolean(ConceptsQueries.hasBroader(id))) {
				model.add(publicationUtils.tranformBaseURIToPublish(RdfUtils.conceptIRI(id)),
						SKOS.TOP_CONCEPT_OF, publicationUtils.tranformBaseURIToPublish(RdfUtils.conceptScheme()),
						RdfUtils.conceptGraph());
			}
		}
	}

	private void publishExplanatoryNotes(RepositoryConnection con, Resource note, Model model) throws RmesException {
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, note);
		try {
			String lg = "";
			String xhtml = "";
			Resource subject = null; 
			Resource graph = null;
			while (statements.hasNext()) {
				Statement st = statements.next();
				String predicat = RdfUtils.toString(st.getPredicate());
				subject = publicationUtils.tranformBaseURIToPublish(st.getSubject());
				graph = st.getContext();
				if (predicat.endsWith("conceptVersion")) {
					// nothing, wouldn't copy this attr
				}
				// Literals
				else {
					if (predicat.endsWith("language")) {
						lg = st.getObject().toString().substring(1, 3);
					}
					if (predicat.endsWith("noteLiteral")) {
						xhtml = st.getObject().toString().substring(1)
								.replace("\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>", "");
					}
					model.add(subject, st.getPredicate(), st.getObject(), st.getContext());
				}
			}
			Literal plainText = RdfUtils.setLiteralString(Jsoup.parse(xhtml).text(), lg);
			if (subject == null) {
				throw new RmesException(HttpStatus.SC_NO_CONTENT, "subject can't be null", "");
			}
			model.add(publicationUtils.tranformBaseURIToPublish(subject), XKOS.PLAIN_TEXT, plainText, graph);
		} catch (RepositoryException e) {
			repoGestion.closeStatements(statements);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);

		}
		repoGestion.closeStatements(statements);
	}

	private void publishMemberLinks(Resource concept, Model model, RepositoryConnection conn) throws RmesException {
		RepositoryResult<Statement> statements = null;
		try {
			statements = conn.getStatements(null, SKOS.MEMBER, concept, false);
		} catch (RepositoryException e) {
			repoGestion.closeStatements(statements);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
		}
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
						publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
			}
		} catch (RepositoryException e) {
			repoGestion.closeStatements(statements);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
		}
		repoGestion.closeStatements(statements);	}

	public void publishCollection(JSONArray collectionsToValidate) throws RmesException {

		for (int i = 0; i < collectionsToValidate.length(); ++i) {
			String collectionId = collectionsToValidate.getString(i);
			Model model = new LinkedHashModel();
			Resource collection = RdfUtils.collectionIRI(collectionId);
			//TODO uncomment when we can notify...
			//Boolean creation = !repositoryPublication.getResponseAsBoolean(CollectionsQueries.isCollectionExist(collectionId));
			RepositoryConnection con = repoGestion.getConnection();
			RepositoryResult<Statement> statements = repoGestion.getStatements(con, collection);

			try {
				try {
					while (statements.hasNext()) {
						Statement st = statements.next();
						// Other URI to transform
						if (RdfUtils.toString(st.getPredicate()).endsWith("member")) {
							model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
									publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
						} else if (RdfUtils.toString(st.getPredicate()).endsWith("isValidated")
								|| (RdfUtils.toString(st.getPredicate()).endsWith(Constants.CREATOR))
								|| (RdfUtils.toString(st.getPredicate()).endsWith(Constants.CONTRIBUTOR))) {
							// nothing, wouldn't copy this attr
						}
						// Literals
						else {
							model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(), st.getObject(),
									st.getContext());
						}
					}
				} catch (RepositoryException e) {
					throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
				}
			} finally {
				repoGestion.closeStatements(statements);
				con.close();
			}
			Resource collectionToPublish = publicationUtils.tranformBaseURIToPublish(collection);
			repositoryPublication.publishResource(collectionToPublish, model, Constants.COLLECTION);
		}
	}

}
