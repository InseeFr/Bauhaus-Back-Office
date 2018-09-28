package fr.insee.rmes.persistance.service.sesame.concepts.publication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.notifications.NotificationsContract;
import fr.insee.rmes.persistance.notifications.RmesNotificationsImpl;
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.service.sesame.ontologies.XKOS;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryPublication;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

public class ConceptsPublication {

	private static final String REPOSITORY_EXCEPTION = "RepositoryException";
	static NotificationsContract notification = new RmesNotificationsImpl();

	private static Resource tranformBaseURIToPublish(Resource resource) {
		String newResource = resource.toString().replace(Config.BASE_URI_GESTION, Config.BASE_URI_PUBLICATION);
		return SesameUtils.toURI(newResource);

	}

	public static void publishConcepts(JSONArray conceptsToPublish) throws RmesException {

		for (int i = 0; i < conceptsToPublish.length(); ++i) {
			String conceptId = conceptsToPublish.getString(i);
			//TODO uncomment when we can notify...
			//Boolean creation = !RepositoryPublication.getResponseAsBoolean(ConceptsQueries.isConceptExist(conceptId));
			Model model = new LinkedHashModel();
			List<Resource> noteToClear = new ArrayList<>();
			List<Resource> topConceptOfToDelete = new ArrayList<>();
			checkTopConceptOf(conceptId, model);
			Resource concept = SesameUtils.conceptIRI(conceptId);

			RepositoryConnection con = RepositoryUtils.getConnection(RepositoryGestion.REPOSITORY_GESTION);
			RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, concept);
			
			String[] notes = {"scopeNote","definition","editorialNote"} ;
			String[] links = {"inScheme","disseminationStatus","references","replaces","related"};
			String[] ignoredAttrs = {"isValidated","changeNote","creator","contributor"};

			try {
				Boolean hasBroader = false;
				Resource subject = null, graph = null;
				while (statements.hasNext()) {
					Statement st = statements.next();
					subject = tranformBaseURIToPublish(st.getSubject());
					graph = st.getContext();
					String predicat = st.getPredicate().toString();
					// Notes, transform URI and get attributs
					if (stringEndsWithItemFromList(predicat,notes)) {
						model.add(subject, st.getPredicate(), tranformBaseURIToPublish((Resource) st.getObject()),
								graph);
						publishExplanatoryNotes(con, SesameUtils.toURI(st.getObject().toString()), model);
						noteToClear.add(tranformBaseURIToPublish((Resource) st.getObject()));
					}
					// Other URI to transform	
					else if (stringEndsWithItemFromList(predicat,links)) {
						model.add(subject, st.getPredicate(), tranformBaseURIToPublish((Resource) st.getObject()),
								graph);
					}
					// Broader links
					else if (predicat.endsWith("broader")) {
						hasBroader = true;
						model.add(subject, st.getPredicate(), tranformBaseURIToPublish((Resource) st.getObject()),
								graph);
						model.add(tranformBaseURIToPublish((Resource) st.getObject()), SKOS.NARROWER, subject, graph);
					}
					// Narrower links
					else if (predicat.endsWith("narrower")) {
						Resource object = tranformBaseURIToPublish((Resource) st.getObject());
						topConceptOfToDelete.add(object);
						model.add(subject, st.getPredicate(), object, graph);
						model.add(object, SKOS.BROADER, subject, graph);
					} else if (stringEndsWithItemFromList(predicat,ignoredAttrs)) {
						// nothing, wouldn't copy this attr
					}
					// Literals
					else {
						model.add(subject, st.getPredicate(), st.getObject(), graph);
					}
				}
				if (!hasBroader) {
					model.add(subject, SKOS.TOP_CONCEPT_OF, tranformBaseURIToPublish(SesameUtils.conceptScheme()),
							graph);
				}
			} catch (RepositoryException e) {
				throw new RmesException(500, e.getMessage(), REPOSITORY_EXCEPTION);

			}
			
			RepositoryGestion.closeStatements(statements);
			publishMemberLinks(concept, model, con);
			
			Resource conceptToPublish = tranformBaseURIToPublish(concept);
			RepositoryPublication.publishConcept(conceptToPublish, model, noteToClear, topConceptOfToDelete);
			//TODO uncomment when we can notify...
			/* if (creation)
			// notification.notifyConceptCreation(conceptId,
			// concept.toString());
			// else notification.notifyConceptUpdate(conceptId,
			// concept.toString());*/
		}

	}
	
	private static boolean stringEndsWithItemFromList(String inputStr, String[] items) {
	    return Arrays.stream(items).parallel().anyMatch(inputStr::endsWith);
	}

	public static void checkTopConceptOf(String conceptId, Model model)  throws RmesException {
		JSONArray conceptsToCheck = RepositoryPublication.getResponseAsArray(ConceptsQueries.getNarrowers(conceptId));
		for (int i = 0; i < conceptsToCheck.length(); i++) {
			String id = conceptsToCheck.getJSONObject(i).getString("narrowerId");
			if (!RepositoryGestion.getResponseAsBoolean(ConceptsQueries.hasBroader(id))) {
				model.add(tranformBaseURIToPublish(SesameUtils.conceptIRI(id)),
						SKOS.TOP_CONCEPT_OF, tranformBaseURIToPublish(SesameUtils.conceptScheme()),
						SesameUtils.conceptGraph());
			}
		}
	}

	public static void publishExplanatoryNotes(RepositoryConnection con, Resource note, Model model) throws RmesException {
		RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, note);
		try {
			String lg = "", xhtml = "";
			Resource subject = null, graph = null;
			while (statements.hasNext()) {
				Statement st = statements.next();
				String predicat = st.getPredicate().toString();
				subject = tranformBaseURIToPublish(st.getSubject());
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
			Literal plainText = SesameUtils.setLiteralString(Jsoup.parse(xhtml).text(), lg);
			if (subject == null) {
				throw new RmesException(HttpStatus.SC_NO_CONTENT, "subject can't be null", "");
			}
			model.add(tranformBaseURIToPublish(subject), XKOS.PLAIN_TEXT, plainText, graph);
		} catch (RepositoryException e) {
			throw new RmesException(500, e.getMessage(), REPOSITORY_EXCEPTION);

		}
	}

	public static void publishMemberLinks(Resource concept, Model model, RepositoryConnection conn) throws RmesException {
		RepositoryResult<Statement> statements = null;
		try {
			statements = conn.getStatements(null, SKOS.MEMBER, concept, false);
		} catch (RepositoryException e) {
			throw new RmesException(500, e.getMessage(), REPOSITORY_EXCEPTION);
		}
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				model.add(tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
						tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
			}
		} catch (RepositoryException e) {
			throw new RmesException(500, e.getMessage(), REPOSITORY_EXCEPTION);
		}
	}

	public static void publishCollection(JSONArray collectionsToValidate) throws RmesException {

		for (int i = 0; i < collectionsToValidate.length(); ++i) {
			String collectionId = collectionsToValidate.getString(i);
			Model model = new LinkedHashModel();
			Resource collection = SesameUtils.collectionIRI(collectionId);
			//TODO uncomment when we can notify...
			//Boolean creation = !RepositoryPublication.getResponseAsBoolean(CollectionsQueries.isCollectionExist(collectionId));
			RepositoryConnection con = RepositoryUtils.getConnection(RepositoryGestion.REPOSITORY_GESTION);
			RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, collection);

			try {
				try {
					while (statements.hasNext()) {
						Statement st = statements.next();
						// Other URI to transform
						if (st.getPredicate().toString().endsWith("member")) {
							model.add(tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
									tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
						} else if (st.getPredicate().toString().endsWith("isValidated")
								|| st.getPredicate().toString().endsWith("creator")
								|| st.getPredicate().toString().endsWith("contributor")) {
							// nothing, wouldn't copy this attr
						}
						// Literals
						else {
							model.add(tranformBaseURIToPublish(st.getSubject()), st.getPredicate(), st.getObject(),
									st.getContext());
						}
					}
				} catch (RepositoryException e) {
					throw new RmesException(500, e.getMessage(), REPOSITORY_EXCEPTION);
				}
			} finally {
				RepositoryGestion.closeStatements(statements);
			}
			Resource collectionToPublish = tranformBaseURIToPublish(collection);
			RepositoryPublication.publishCollection(collectionToPublish, model);
			// if (creation)
			// notification.notifyCollectionCreation(collectionId,
			// collection.toString());
			// else notification.notifyCollectionUpdate(collectionId,
			// collection.toString());
		}
	}

}
