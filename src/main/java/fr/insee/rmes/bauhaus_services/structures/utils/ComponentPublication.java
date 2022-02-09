package fr.insee.rmes.bauhaus_services.structures.utils;

import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.stereotype.Repository;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesException;

@Repository
public class ComponentPublication extends RdfService {

	public void publishComponent(Resource component, IRI type) throws RmesException {
		
		Model model = new LinkedHashModel();
		//TODO notify...
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, component);

		try {	
			try {
				while (statements.hasNext()) {
					Statement st = statements.next();
					String pred = RdfUtils.toString(st.getPredicate());
					if (pred.endsWith("validationState") || pred.endsWith(Constants.CONTRIBUTOR) || pred.endsWith(Constants.CREATOR)) {
						// nothing, wouldn't copy this attr
					}else if (pred.endsWith("attribute")
							|| pred.endsWith("dimension")
							|| pred.endsWith("measure")
							|| pred.endsWith("codeList")
							|| pred.endsWith("concept")
							|| pred.endsWith("range")) {
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
								PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
					}
					else {
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()),
								st.getPredicate(),
								st.getObject(),
								st.getContext());
					}

				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			repoGestion.closeStatements(statements);
			con.close();
		}
		Resource componentToPublishRessource = PublicationUtils.tranformBaseURIToPublish(component);
		RepositoryPublication.publishResource(componentToPublishRessource, model, RdfUtils.toString(type));
		
	}

}

