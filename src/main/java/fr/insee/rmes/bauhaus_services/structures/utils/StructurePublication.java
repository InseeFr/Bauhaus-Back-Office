package fr.insee.rmes.bauhaus_services.structures.utils;

import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.stereotype.Repository;

@Repository
public class StructurePublication extends RdfService {

	public void publish(Resource structure) throws RmesException {
		
		Model model = new LinkedHashModel();
		//TODO notify...
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, structure);

		try {	
			try {
				while (statements.hasNext()) {
					Statement st = statements.next();
					String pred = ((SimpleIRI) st.getPredicate()).toString();

					if (pred.endsWith("validationState")) {
						// nothing, wouldn't copy this attr
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
		}
		Resource componentToPublishRessource = PublicationUtils.tranformBaseURIToPublish(structure);
		RepositoryPublication.publishResource(componentToPublishRessource, model, "Structure");
		
	}

}

