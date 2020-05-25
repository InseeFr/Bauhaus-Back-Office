package fr.insee.rmes.bauhaus_services.operations.operations;

import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.external_services.notifications.NotificationsContract;
import fr.insee.rmes.external_services.notifications.RmesNotificationsImpl;


@Repository
public class OperationPublication extends RdfService{
	
	@Autowired
	static FamOpeSerUtils famOpeSerUtils;

	static NotificationsContract notification = new RmesNotificationsImpl();

	String[] ignoredAttrs = { "isValidated", "changeNote", "creator", "contributor" };

	public void publishOperation(String operationId) throws RmesException {
		OperationsUtils operationsUtils = new OperationsUtils();
		Model model = new LinkedHashModel();
		String[] ignoredAttrs = { "validationState", "hasPart", "creator", "contributor" };

		Resource operation = RdfUtils.operationIRI(operationId);
		JSONObject operationJson = operationsUtils.getOperationById(operationId);
		
		checkSeriesIsPublished(operationId, operationJson);

		RepositoryConnection con = PublicationUtils.getRepositoryConnectionGestion();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, operation);

		try {
			if (!statements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.OPERATION_UNKNOWN_ID, "Operation not found", operationId);
			}
			while (statements.hasNext()) {
				Statement st = statements.next();
				// Other URI to transform
				if (st.getPredicate().toString().endsWith("isPartOf")) {
					model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
							PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
				} else if (PublicationUtils.stringEndsWithItemFromList(st.getPredicate().toString(), ignoredAttrs)) {
					// nothing, wouldn't copy this attr
				}
				// Literals
				else {
					model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
							st.getObject(), st.getContext());
				}
				addHasPartStatements(model, operation, con);
			}
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Config.REPOSITORY_EXCEPTION);
		}
		finally {
			repoGestion.closeStatements(statements);
		}
		Resource operationToPublishRessource = PublicationUtils.tranformBaseURIToPublish(operation);
		RepositoryPublication.publishResource(operationToPublishRessource, model, "operation");

	}

	private static void checkSeriesIsPublished(String operationId, JSONObject operationJson)
			throws RmesException {
		String seriesId = operationJson.getJSONObject("series").getString(Constants.ID);
		String status = famOpeSerUtils.getValidationStatus(seriesId);

		if (PublicationUtils.isPublished(status)) {
			throw new RmesUnauthorizedException(ErrorCodes.OPERATION_VALIDATION_UNPUBLISHED_SERIES,
					"This operation cannot be published before its series is published",
					"Operation: " + operationId + " ; Series: " + seriesId);
		}
	}

	private void addHasPartStatements(Model model, Resource operation, RepositoryConnection con)
			throws RmesException, RepositoryException {
		RepositoryResult<Statement> hasPartStatements = repoGestion.getHasPartStatements(con, operation);

		while (hasPartStatements.hasNext()) {
			Statement hpst = hasPartStatements.next();
			model.add(PublicationUtils.tranformBaseURIToPublish(hpst.getSubject()), hpst.getPredicate(),
					PublicationUtils.tranformBaseURIToPublish((Resource) hpst.getObject()), hpst.getContext());
		}
	}

}
