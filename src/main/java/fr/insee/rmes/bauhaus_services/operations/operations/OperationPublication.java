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
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;


@Component
public class OperationPublication extends RdfService{
	
	@Autowired
	private FamOpeSerIndUtils famOpeSerUtils;
	
	@Autowired
	private OperationsUtils operationsUtils;

	String[] ignoredAttrs = { "validationState", "hasPart", Constants.PUBLISHER, "contributor" };

	public void publishOperation(String operationId) throws RmesException {
		Model model = new LinkedHashModel();

		Resource operation = RdfUtils.operationIRI(operationId);
		JSONObject operationJson = operationsUtils.getOperationJsonById(operationId);
		
		checkSeriesIsPublished(operationId, operationJson);

		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, operation);

		try {
			if (!statements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.OPERATION_UNKNOWN_ID, "Operation not found", operationId);
			}
			while (statements.hasNext()) {
				Statement st = statements.next();
				// Other URI to transform
				if (RdfUtils.toString(st.getPredicate()).endsWith("isPartOf")) {
					model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
							PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
				} else if (PublicationUtils.stringEndsWithItemFromList(RdfUtils.toString(st.getPredicate()), ignoredAttrs)) {
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
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
		}
		finally {
			repoGestion.closeStatements(statements);
			con.close();
		}
		Resource operationToPublishRessource = PublicationUtils.tranformBaseURIToPublish(operation);
		RepositoryPublication.publishResource(operationToPublishRessource, model, "operation");

	}

	private void checkSeriesIsPublished(String operationId, JSONObject operationJson)
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
			throws RmesException {
		RepositoryResult<Statement> hasPartStatements = repoGestion.getHasPartStatements(con, operation);

		while (hasPartStatements.hasNext()) {
			Statement hpst = hasPartStatements.next();
			model.add(PublicationUtils.tranformBaseURIToPublish(hpst.getSubject()), hpst.getPredicate(),
					PublicationUtils.tranformBaseURIToPublish((Resource) hpst.getObject()), hpst.getContext());
		}
	}

}
