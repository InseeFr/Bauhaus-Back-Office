package fr.insee.rmes.bauhaus_services.operations.documentations;

import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.external_services.notifications.NotificationsContract;
import fr.insee.rmes.external_services.notifications.RmesNotificationsImpl;

@Repository
public class DocumentationPublication extends RdfService {

	@Autowired
	static RepositoryUtils repoUtils;

	static NotificationsContract notification = new RmesNotificationsImpl();

	public void publishSims(String simsId) throws RmesException {

		Model model = new LinkedHashModel();
		Resource sims = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, simsId);
		Resource graph = RdfUtils.simsGraph(simsId);

		// TODO notify...
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> metadataReportStatements = repoGestion.getCompleteGraph(con, graph);

		try {
			if (!metadataReportStatements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_ID, "Sims not found", simsId);
			}
			while (metadataReportStatements.hasNext()) {
				Statement st = metadataReportStatements.next();
				// Triplets that don't get published
				if (st.getPredicate().toString().endsWith("validationState")) {
					// nothing, wouldn't copy this attr
				} else {
					Resource subject = PublicationUtils.tranformBaseURIToPublish(st.getSubject());
					IRI predicate = RdfUtils
							.createIRI(PublicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
					Value object = st.getObject();
					if (st.getObject() instanceof Resource) {
						object = PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject());
					}

					model.add(subject, predicate, object, st.getContext());
				}
			}
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}

		finally {
			repoGestion.closeStatements(metadataReportStatements);
		}

		RepositoryPublication.publishContext(graph, model, "sims");

	}

}
