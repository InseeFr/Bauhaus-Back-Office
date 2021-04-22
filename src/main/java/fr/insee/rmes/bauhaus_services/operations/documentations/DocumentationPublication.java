package fr.insee.rmes.bauhaus_services.operations.documentations;

import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsPublication;
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
	
	@Autowired 
	DocumentsPublication documentsPublication;

	static final String[] rubricsNotForPublication = {"S.1.3","S.1.4","S.1.5","S.1.6","S.1.7","S.1.8","validationState"};

	public void publishSims(String simsId) throws RmesException {

		Model model = new LinkedHashModel();
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
				String predicate = ((SimpleIRI)st.getPredicate()).toString();
				if (!isTripletForPublication(predicate)) {
					// nothing, wouldn't copy this attr
				} else {
					Resource subject = PublicationUtils.tranformBaseURIToPublish(st.getSubject());
					IRI predicateIRI = RdfUtils
							.createIRI(PublicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
					Value object = st.getObject();
					if (st.getObject() instanceof Resource) {
						object = PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject());
					}

					model.add(subject, predicateIRI, object, st.getContext());
				}
			}
			documentsPublication.publishAllDocumentsInSims(simsId);
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}

		finally {
			repoGestion.closeStatements(metadataReportStatements);
		}

		RepositoryPublication.publishContext(graph, model, "sims");

	}

	private boolean isTripletForPublication(String predicate) {
		for(String rubric : rubricsNotForPublication) {
			if (predicate.endsWith(rubric)) return false;}
		return true;
	}

}
