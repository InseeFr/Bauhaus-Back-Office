package fr.insee.rmes.bauhaus_services.structures.utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
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

@Repository
public class ComponentPublication extends RdfService {

	public void publishComponent(Resource component, IRI type) throws RmesException {
		
		Model model = new LinkedHashModel();
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, component);

		try {	
			try {
				while (statements.hasNext()) {
					Statement st = statements.next();
                    String pred = st.getPredicate().toString();
					if (pred.endsWith("validationState") || pred.endsWith(Constants.CONTRIBUTOR) || pred.endsWith(Constants.CREATOR)) {
						// nothing, wouldn't copy this attr
					}else if (pred.endsWith("attribute")
							|| pred.endsWith("dimension")
							|| pred.endsWith("measure")
							|| pred.endsWith(Constants.CODELIST)
							|| pred.endsWith(Constants.CONCEPT)
							|| pred.endsWith("range")) {
						model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()), st.getPredicate(),
								publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), st.getContext());
					}
					else {
						model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()),
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
		Resource componentToPublishRessource = publicationUtils.tranformBaseURIToPublish(component);
        repositoryPublication.publishResource(componentToPublishRessource, model, type.toString());
		
	}

}

