package fr.insee.rmes.bauhaus_services.operations.documentations;

import org.apache.http.HttpStatus;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdfUtils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdfUtils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdfUtils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdfUtils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.rdfUtils.RepositoryUtils;
import fr.insee.rmes.bauhaus_services.rdfUtils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.external_services.notifications.NotificationsContract;
import fr.insee.rmes.external_services.notifications.RmesNotificationsImpl;

public class DocumentationPublication {
	
	@Autowired
	static RepositoryUtils repoUtils;

	static NotificationsContract notification = new RmesNotificationsImpl();

	public static void publishSims(String simsId) throws RmesException {
		
		Model model = new LinkedHashModel();
		Resource sims= RdfUtils.objectIRI(ObjectType.DOCUMENTATION,simsId);
		Resource graph = RdfUtils.simsGraph(simsId);
	
		//TODO notify...
		RepositoryConnection con = repoUtils.getConnection(RepositoryGestion.REPOSITORY_GESTION);
		RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, sims);

		RepositoryResult<Statement> metadataReportStatements = RepositoryGestion.getMetadataReportStatements(con, sims, graph);

		
		try {	
			try {
				if (!statements.hasNext()) {
					throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_ID,"Sims not found", simsId);
				}
				while (statements.hasNext()) {
					Statement st = statements.next();
					// Triplets that don't get published
					if ( st.getPredicate().toString().endsWith("validationState")
							|| st.getPredicate().toString().endsWith("creator")
							|| st.getPredicate().toString().endsWith("contributor")) {
						// nothing, wouldn't copy this attr
					} else if (
							// Other URI to transform : 
							st.getPredicate().toString().endsWith("target")
							|| st.getPredicate().toString().endsWith("additionalMaterial") 
							|| st.getPredicate().toString().endsWith("metadataReport"))
					{
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), 
							st.getPredicate(), 
							PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
							st.getContext());
					}
					
					// Literals
					else {
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), 
								st.getPredicate(), 
								st.getObject(),
								st.getContext());
					}
				}
				while (metadataReportStatements.hasNext()) {
					Statement st = metadataReportStatements.next();
					model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), 
							st.getPredicate(), 
							PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
							st.getContext());
				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			RepositoryGestion.closeStatements(statements);
		}
		
		Resource simsToPublishRessource = PublicationUtils.tranformBaseURIToPublish(sims);
		RepositoryPublication.publishResource(simsToPublishRessource, model, "sims");
		
	}

	
}
