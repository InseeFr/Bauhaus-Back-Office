package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
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
import org.springframework.stereotype.Component;


@Component
public class ClassificationPublication extends RdfService{
	
	String[] ignoredAttrs = { "isValidated", "validationState", "conceptVersion" };

	public void publishClassification(Resource graphIri) throws RmesException {
		Model model = new LinkedHashModel();

		// TODO notify...
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> classifStatements = repoGestion.getCompleteGraph(con, graphIri);

		try {
			if (!classifStatements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.CLASSIFICATION_UNKNOWN_ID, "Classification not found", graphIri.stringValue());
			}
			transformStatementToPublish(model, classifStatements);
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}

		finally {
			repoGestion.closeStatements(classifStatements);
			con.close();
		}

		repositoryPublication.publishContext(graphIri, model, "classification");

	}

	public void transformStatementToPublish(Model model, RepositoryResult<Statement> classifStatements) {
		while (classifStatements.hasNext()) {
			Statement st = classifStatements.next();
			// Triplets that don't get published
            String predicate = st.getPredicate().toString();
			if (!isTripletForPublication(predicate)) {
				// nothing, wouldn't copy this attr
			} else {
				transformTripleToPublish(model, st);
			}
		}
		
	}


	private boolean isTripletForPublication(String predicate) {
		return !PublicationUtils.stringEndsWithItemFromList(predicate, ignoredAttrs);
	}


}
