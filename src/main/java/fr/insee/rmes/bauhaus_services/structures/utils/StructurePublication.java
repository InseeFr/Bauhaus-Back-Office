package fr.insee.rmes.bauhaus_services.structures.utils;

import java.util.Arrays;

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
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesException;

@Repository
public class StructurePublication extends RdfService {


	private void copyTriplet(Resource structure, Model model, RepositoryConnection con, String[] denyList) throws RmesException {
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, structure);

		try {
			try {

				while (statements.hasNext()) {
					Statement st = statements.next();
					String pred = ((SimpleIRI) st.getPredicate()).toString();
					if (Arrays.stream(denyList).anyMatch(entry -> pred.endsWith(entry))) {
						// nothing, wouldn't copy this attr
					} else if(pred.endsWith("component")){
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()),
								st.getPredicate(),
								PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
								st.getContext());

						copyTriplet((Resource) st.getObject(), model, con, new String[]{"identifier", "created", "modified"});
					} else if(pred.endsWith("attribute") || pred.endsWith("measure") || pred.endsWith("dimension")){
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()),
								st.getPredicate(),
								PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
								st.getContext());
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
	}

	public void publish(Resource structure) throws RmesException {
		
		Model model = new LinkedHashModel();
		RepositoryConnection con = repoGestion.getConnection();

		this.copyTriplet(structure, model, con, new String[]{"validationState", Constants.CREATOR, Constants.CONTRIBUTOR});
		con.close();
		Resource structureToPublish = PublicationUtils.tranformBaseURIToPublish(structure);

		RepositoryPublication.clearStructureAndComponentForAllRepositories(structureToPublish);
		RepositoryPublication.publishResource(structureToPublish, model, "Structure");

	}

}

