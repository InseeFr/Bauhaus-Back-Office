package fr.insee.rmes.bauhaus_services.operations.families;

import org.apache.http.HttpStatus;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdfUtils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdfUtils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdfUtils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.rdfUtils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.external_services.notifications.NotificationsContract;
import fr.insee.rmes.external_services.notifications.RmesNotificationsImpl;

public class FamilyPublication {

	static NotificationsContract notification = new RmesNotificationsImpl();

	public static void publishFamily(String familyId) throws RmesException {
		
		Model model = new LinkedHashModel();
		Resource family = RdfUtils.familyIRI(familyId);
		//TODO notify...
		RepositoryConnection con = PublicationUtils.getRepositoryConnectionGestion();
		RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, family);

		try {	
			try {
				if (!statements.hasNext()) {
					throw new RmesNotFoundException(ErrorCodes.FAMILY_UNKNOWN_ID,"Family not found", familyId);
				}
				while (statements.hasNext()) {
					Statement st = statements.next();
					// Triplets that don't get published
					if (st.getPredicate().toString().endsWith("isValidated")
							|| st.getPredicate().toString().endsWith("validationState")
							|| st.getPredicate().toString().endsWith("hasPart")
							|| st.getPredicate().toString().endsWith("creator")
							|| st.getPredicate().toString().endsWith("contributor")) {
						// nothing, wouldn't copy this attr
					}
					// Literals
					else {
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), 
								st.getPredicate(), 
								st.getObject(),
								st.getContext());
					}
					// Other URI to transform : none
				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			RepositoryGestion.closeStatements(statements);
		}
		Resource familyToPublishRessource = PublicationUtils.tranformBaseURIToPublish(family);
		RepositoryPublication.publishResource(familyToPublishRessource, model, "family");
		
	}

}

