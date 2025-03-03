package fr.insee.rmes.bauhaus_services.operations.families;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
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
import org.springframework.stereotype.Repository;

@Repository
public class FamilyPublication extends RdfService {

	public void publishFamily(String familyId) throws RmesException {
		
		Model model = new LinkedHashModel();
		Resource family = RdfUtils.familyIRI(familyId);
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, family);

		try {	
			try {
				if (!statements.hasNext()) {
					throw new RmesNotFoundException(ErrorCodes.FAMILY_UNKNOWN_ID,"Family not found", familyId);
				}
				while (statements.hasNext()) {
					Statement st = statements.next();
					String pred = RdfUtils.toString(st.getPredicate());
					// Triplets that don't get published
					if (pred.endsWith("isValidated")
							|| pred.endsWith("validationState")
							|| pred.endsWith("hasPart")
							|| pred.endsWith(Constants.PUBLISHER)
							|| pred.endsWith(Constants.CONTRIBUTOR)) {
						// nothing, wouldn't copy this attr
					}
					// Literals
					else {
						model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()), 
								st.getPredicate(), 
								st.getObject(),
								st.getContext());
					}
					// Other URI to transform : none
				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			repoGestion.closeStatements(statements);
			con.close();
		}
		Resource familyToPublishRessource = publicationUtils.tranformBaseURIToPublish(family);
		repositoryPublication.publishResource(familyToPublishRessource, model, Constants.FAMILY);
		
	}

}

