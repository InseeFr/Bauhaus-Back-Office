package fr.insee.rmes.bauhaus_services.operations.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
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


@Component
public class OperationPublication extends RdfService{

	@Autowired
	ParentUtils ownersUtils;


	String[] ignoredAttrs = { "validationState", "hasPart", Constants.PUBLISHER, Constants.CONTRIBUTOR };

	public void publishOperation(String operationId, JSONObject operationJson) throws RmesException {
		checkSeriesIsPublished(operationId, operationJson);

		Model model = new LinkedHashModel();

		Resource operation = RdfUtils.operationIRI(operationId);
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
					model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
							publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
				} else if (PublicationUtils.stringEndsWithItemFromList(RdfUtils.toString(st.getPredicate()), ignoredAttrs)) {
					// nothing, wouldn't copy this attr
				}
				// Literals
				else {
					model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
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
		Resource operationToPublishRessource = publicationUtils.tranformBaseURIToPublish(operation);
		repositoryPublication.publishResource(operationToPublishRessource, model, "operation");

	}

	private void checkSeriesIsPublished(String operationId, JSONObject operationJson)
			throws RmesException {
		String seriesId = operationJson.getJSONObject("series").getString(Constants.ID);
		String status = ownersUtils.getValidationStatus(seriesId);

		if (PublicationUtils.isUnublished(status)) {
			throw new RmesBadRequestException(ErrorCodes.OPERATION_VALIDATION_UNPUBLISHED_PARENT,
					"This operation cannot be published before its series is published",
					"Operation: " + operationId + " ; Series: " + seriesId);
		}
	}

	private void addHasPartStatements(Model model, Resource operation, RepositoryConnection con)
			throws RmesException {
		RepositoryResult<Statement> hasPartStatements = repoGestion.getHasPartStatements(con, operation);

		while (hasPartStatements.hasNext()) {
			Statement hpst = hasPartStatements.next();
			model.add(publicationUtils.tranformBaseURIToPublish(hpst.getSubject()), hpst.getPredicate(),
					publicationUtils.tranformBaseURIToPublish((Resource) hpst.getObject()), hpst.getContext());
		}
	}

}
