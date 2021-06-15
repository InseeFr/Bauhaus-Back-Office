package fr.insee.rmes.bauhaus_services.operations.indicators;

import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.stereotype.Repository;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.external_services.notifications.NotificationsContract;
import fr.insee.rmes.external_services.notifications.RmesNotificationsImpl;

@Repository
public class IndicatorPublication extends RdfService {

	static NotificationsContract notification = new RmesNotificationsImpl();

	public void publishIndicator(String indicatorId) throws RmesException {

		Model model = new LinkedHashModel();
		Resource indicator = RdfUtils.objectIRI(ObjectType.INDICATOR, indicatorId);

		// TODO notify...
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, indicator);

		try {
			if (!statements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.INDICATOR_UNKNOWN_ID, "Indicator not found", indicatorId);
			}
			while (statements.hasNext()) {
				Statement st = statements.next();
				// Triplets that don't get published
				String pred = ((SimpleIRI) st.getPredicate()).toString();
				
				if (pred.endsWith("isValidated")
						|| pred.endsWith("validationState")) {
					// nothing, wouldn't copy this attr
				} else if (pred.endsWith("wasGeneratedBy")
						|| pred.endsWith("seeAlso")
						|| pred.endsWith("replaces")
						|| pred.endsWith("isReplacedBy")
						|| pred.endsWith("contributor")
						|| pred.endsWith("publisher")
						|| pred.endsWith("accrualPeriodicity")) {
					model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
							PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
				}
				// Literals
				else {
					model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
							st.getObject(), st.getContext());
				}
				// Other URI to transform : none
			}
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}

		finally {
			repoGestion.closeStatements(statements);
			con.close();
		}
		Resource indicatorToPublishRessource = PublicationUtils.tranformBaseURIToPublish(indicator);
		RepositoryPublication.publishResource(indicatorToPublishRessource, model, "indicator");

	}

}
