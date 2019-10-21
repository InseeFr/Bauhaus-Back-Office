package fr.insee.rmes.persistance.service.sesame.operations.operations;

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
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.notifications.NotificationsContract;
import fr.insee.rmes.persistance.notifications.RmesNotificationsImpl;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.persistance.service.sesame.operations.families.FamiliesUtils;
import fr.insee.rmes.persistance.service.sesame.operations.series.SeriesUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryPublication;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

public class OperationPublication {

static NotificationsContract notification = new RmesNotificationsImpl();
	
	public static void publishOperation(String operationId) throws RmesException {
		OperationsUtils operationsUtils= new OperationsUtils();
		SeriesUtils seriesUtils= new SeriesUtils();
		Model model = new LinkedHashModel();
		Resource operation = SesameUtils.operationIRI(operationId);
		JSONObject operationJson = operationsUtils.getOperationById(operationId);
		String seriesId = operationJson.getJSONObject("series").getString("id");
		String status=FamOpeSerUtils.getValidationStatus(seriesId);
		
		if(status.equals(INSEE.UNPUBLISHED) | status.equals("UNDEFINED")) {
			throw new RmesUnauthorizedException("This operation cannot be published before its series is published", 
					"Operation: "+operationId+" ; Series: "+seriesId);
		}
		
		RepositoryConnection con = RepositoryUtils.getConnection(RepositoryGestion.REPOSITORY_GESTION);
		RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, operation);

		RepositoryResult<Statement> hasPartStatements = RepositoryGestion.getHasPartStatements(con, operation);
		
		try {	
			try {
				if (!statements.hasNext()) throw new RmesNotFoundException("Operation not found", operationId);
				while (statements.hasNext()) {
					Statement st = statements.next();
					// Other URI to transform
					if (st.getPredicate().toString().endsWith("isPartOf")) {
						model.add(FamOpeSerUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
								FamOpeSerUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
					} else if (st.getPredicate().toString().endsWith("isValidated")
							|| st.getPredicate().toString().endsWith("validationState")
							|| st.getPredicate().toString().endsWith("hasPart")
							|| st.getPredicate().toString().endsWith("creator")
							|| st.getPredicate().toString().endsWith("contributor")) {
						// nothing, wouldn't copy this attr
					}
					// Literals
					else {
						model.add(FamOpeSerUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(), st.getObject(),
								st.getContext());
					}
					while (hasPartStatements.hasNext()) {
						Statement hpst = hasPartStatements.next();
						model.add(FamOpeSerUtils.tranformBaseURIToPublish(hpst.getSubject()), 
								hpst.getPredicate(), 
								FamOpeSerUtils.tranformBaseURIToPublish((Resource) hpst.getObject()),
								hpst.getContext());
					}
				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Config.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			RepositoryGestion.closeStatements(statements);
		}
		Resource operationToPublishRessource = FamOpeSerUtils.tranformBaseURIToPublish(operation);
		RepositoryPublication.publishOperation(operationToPublishRessource, model);
		
	}
	
}
