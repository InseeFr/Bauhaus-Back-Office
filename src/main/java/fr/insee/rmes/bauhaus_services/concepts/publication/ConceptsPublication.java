package fr.insee.rmes.bauhaus_services.concepts.publication;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.notifications.NotificationsContract;
import fr.insee.rmes.external_services.notifications.RmesNotificationsImpl;
import fr.insee.rmes.persistance.ontologies.XKOS;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;

@Component
public class ConceptsPublication extends RdfService{


	static NotificationsContract notification = new RmesNotificationsImpl();

	public void publishConcepts(JSONArray conceptsToPublish) throws RmesException {
		for (int i = 0; i < conceptsToPublish.length(); ++i) {
			String conceptId = conceptsToPublish.getString(i);
			//TODO uncomment when we can notify...
			//Boolean creation = !RepositoryPublication.getResponseAsBoolean(ConceptsQueries.isConceptExist(conceptId));
			Model model = new LinkedHashModel();
			List<Resource> noteToClear = new ArrayList<>();
			List<Resource> topConceptOfToDelete = new ArrayList<>();
			checkTopConceptOf(conceptId, model);
			Resource concept = RdfUtils.conceptIRI(conceptId);

			RepositoryConnection con = repoGestion.getConnection();
			RepositoryResult<Statement> statements = repoGestion.getStatements(con, concept);
			
			String[] notes = {"scopeNote","definition","editorialNote"} ;
			String[] links = {"inScheme","disseminationStatus","references","replaces","related"};
			String[] ignoredAttrs = {"isValidated","changeNote",Constants.CREATOR,"contributor"};

			try {
				boolean hasBroader = false;
				Resource subject = null;
				Resource graph = null;
				while (statements.hasNext()) {
					Statement st = statements.next();
					subject = PublicationUtils.tranformBaseURIToPublish(st.getSubject());
					graph = st.getContext();
					String predicat = ((SimpleIRI)st.getPredicate()).toString();
					// Notes, transform URI and get attributs
					if (PublicationUtils.stringEndsWithItemFromList(predicat,notes)) {
						model.add(subject, st.getPredicate(), PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
								graph);
						publishExplanatoryNotes(con, RdfUtils.toURI(st.getObject().toString()), model);
						noteToClear.add(PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()));
					}
					// Other URI to transform	
					else if (PublicationUtils.stringEndsWithItemFromList(predicat,links)) {
						model.add(subject, st.getPredicate(), PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
								graph);
					}
					// Broader links
					else if (predicat.endsWith("broader")) {
						hasBroader = true;
						model.add(subject, st.getPredicate(), PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
								graph);
						model.add(PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), SKOS.NARROWER, subject, graph);
					}
					// Narrower links
					else if (predicat.endsWith("narrower")) {
						Resource object = PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject());
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
				}
				if (!hasBroader) {
					model.add(subject, SKOS.TOP_CONCEPT_OF, PublicationUtils.tranformBaseURIToPublish(RdfUtils.conceptScheme()),
							graph);
				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
			
			repoGestion.closeStatements(statements);
			publishMemberLinks(concept, model, con);
			
			Resource conceptToPublish = PublicationUtils.tranformBaseURIToPublish(concept);
			RepositoryPublication.publishConcept(conceptToPublish, model, noteToClear, topConceptOfToDelete);
			//TODO uncomment when we can notify...
			/* if (creation)
			// notification.notifyConceptCreation(conceptId,
			// concept.toString());
			// else notification.notifyConceptUpdate(conceptId,
			// concept.toString());*/
		}

	}
	


	public void checkTopConceptOf(String conceptId, Model model)  throws RmesException {
		JSONArray conceptsToCheck = RepositoryPublication.getResponseAsArray(ConceptsQueries.getNarrowers(conceptId));
		for (int i = 0; i < conceptsToCheck.length(); i++) {
			String id = conceptsToCheck.getJSONObject(i).getString("narrowerId");
			if (!repoGestion.getResponseAsBoolean(ConceptsQueries.hasBroader(id))) {
				model.add(PublicationUtils.tranformBaseURIToPublish(RdfUtils.conceptIRI(id)),
						SKOS.TOP_CONCEPT_OF, PublicationUtils.tranformBaseURIToPublish(RdfUtils.conceptScheme()),
						RdfUtils.conceptGraph());
			}
		}
	}

	public void publishExplanatoryNotes(RepositoryConnection con, Resource note, Model model) throws RmesException {
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, note);
		try {
			String lg = "";
			String xhtml = "";
			Resource subject = null; 
			Resource graph = null;
			while (statements.hasNext()) {
				Statement st = statements.next();
				String predicat = ((SimpleIRI)st.getPredicate()).toString();
				subject = PublicationUtils.tranformBaseURIToPublish(st.getSubject());
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
			model.add(PublicationUtils.tranformBaseURIToPublish(subject), XKOS.PLAIN_TEXT, plainText, graph);
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);

		}
	}

	public void publishMemberLinks(Resource concept, Model model, RepositoryConnection conn) throws RmesException {
		RepositoryResult<Statement> statements = null;
		try {
			statements = conn.getStatements(null, SKOS.MEMBER, concept, false);
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
		}
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
						PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
			}
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
		}
	}

	public void publishCollection(JSONArray collectionsToValidate) throws RmesException {

		for (int i = 0; i < collectionsToValidate.length(); ++i) {
			String collectionId = collectionsToValidate.getString(i);
			Model model = new LinkedHashModel();
			Resource collection = RdfUtils.collectionIRI(collectionId);
			//TODO uncomment when we can notify...
			//Boolean creation = !RepositoryPublication.getResponseAsBoolean(CollectionsQueries.isCollectionExist(collectionId));
			RepositoryConnection con = repoGestion.getConnection();
			RepositoryResult<Statement> statements = repoGestion.getStatements(con, collection);

			try {
				try {
					while (statements.hasNext()) {
						Statement st = statements.next();
						// Other URI to transform
						if (((SimpleIRI)st.getPredicate()).toString().endsWith("member")) {
							model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
									PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
						} else if (((SimpleIRI)st.getPredicate()).toString().endsWith("isValidated")
								|| ((SimpleIRI)st.getPredicate()).toString().endsWith(Constants.CREATOR)
								|| ((SimpleIRI)st.getPredicate()).toString().endsWith("contributor")) {
							// nothing, wouldn't copy this attr
						}
						// Literals
						else {
							model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(), st.getObject(),
									st.getContext());
						}
					}
				} catch (RepositoryException e) {
					throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
				}
			} finally {
				repoGestion.closeStatements(statements);
			}
			Resource collectionToPublish = PublicationUtils.tranformBaseURIToPublish(collection);
			RepositoryPublication.publishResource(collectionToPublish, model, "collection");
			// if (creation)
			// notification.notifyCollectionCreation(collectionId,
			// collection.toString());
			// else notification.notifyCollectionUpdate(collectionId,
			// collection.toString());
		}
	}

}
