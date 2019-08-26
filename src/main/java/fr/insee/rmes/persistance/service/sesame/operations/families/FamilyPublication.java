package fr.insee.rmes.persistance.service.sesame.operations.families;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.notifications.NotificationsContract;
import fr.insee.rmes.persistance.notifications.RmesNotificationsImpl;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryPublication;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

public class FamilyPublication {

	private static final String REPOSITORY_EXCEPTION = "RepositoryException";
	static NotificationsContract notification = new RmesNotificationsImpl();

	private static Resource tranformBaseURIToPublish(Resource resource) {
		String newResource = resource.toString().replace(Config.BASE_URI_GESTION, Config.BASE_URI_PUBLICATION);
		return SesameUtils.toURI(newResource);

	}

	
	public static void publishFamily(String familyId) throws RmesException {
		
		Model model = new LinkedHashModel();
		Resource family = SesameUtils.familyIRI(familyId);
		//TODO notify...
		RepositoryConnection con = RepositoryUtils.getConnection(RepositoryGestion.REPOSITORY_GESTION);
		RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, family);

		try {
			try {
				while (statements.hasNext()) {
					Statement st = statements.next();
					// Other URI to transform
					if (st.getPredicate().toString().endsWith("member")) {
						model.add(tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
								tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
					} else if (st.getPredicate().toString().endsWith("isValidated")
							|| st.getPredicate().toString().endsWith("validationState")
							|| st.getPredicate().toString().endsWith("hasPart")
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
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), REPOSITORY_EXCEPTION);
			}
		} finally {
			RepositoryGestion.closeStatements(statements);
		}
		Resource familyToPublishRessource = tranformBaseURIToPublish(family);
		RepositoryPublication.publishFamily(familyToPublishRessource, model);
		
	}

}

