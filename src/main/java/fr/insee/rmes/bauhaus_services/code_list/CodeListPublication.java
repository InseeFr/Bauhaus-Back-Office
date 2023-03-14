package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
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
public class CodeListPublication extends RdfService {

	public void publishCodeList(Resource codelist, boolean partial) throws RmesException {
		
		Model model = new LinkedHashModel();

		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, codelist);

		try {	
			try {
				if (!statements.hasNext()) {
					throw new RmesNotFoundException(ErrorCodes.CODE_LIST_UNKNOWN_ID, "CodeList not found", codelist.stringValue());
				}
				while (statements.hasNext()) {
					Statement st = statements.next();
					String pred = RdfUtils.toString(st.getPredicate());
					String value = st.getObject().stringValue();

					if(!partial){
						if (pred.contains("rdf:_")
								|| value.contains("Seq")
								|| pred.endsWith("validationState")
								|| pred.endsWith(Constants.CREATOR)
								|| pred.endsWith(Constants.CONTRIBUTOR)) {
							continue;
						}
					} else {
						if (pred.endsWith("validationState")
								|| pred.endsWith(Constants.CREATOR)
								|| pred.endsWith(Constants.CONTRIBUTOR)) {
							continue;
						}
					}

					model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()),
							st.getPredicate(),
							st.getObject(),
							st.getContext());
				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			repoGestion.closeStatements(statements);
			con.close();
		}
		Resource codelistToPublishRessource = PublicationUtils.tranformBaseURIToPublish(codelist);
		RepositoryPublication.publishResource(codelistToPublishRessource, model, Constants.CODELIST);
		
	}

}

