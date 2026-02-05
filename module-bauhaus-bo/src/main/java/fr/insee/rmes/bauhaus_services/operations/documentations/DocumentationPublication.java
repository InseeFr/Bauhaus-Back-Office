package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
public class DocumentationPublication extends RdfService {

	@Autowired
	DocumentsPublication documentsPublication;

	@Value("${fr.insee.rmes.bauhaus.documentation.geographie.baseURI}")
	private String documentationsGeoBaseUri;

	@Value("${fr.insee.rmes.bauhaus.organisations.graph}")
	private String organisationsGraph;

	static final Set<String> RUBRICS_NOT_FOR_PUBLICATION = Set.of("S.1.3","S.1.4","S.1.5","S.1.6","S.1.7","S.1.8","validationState");

	public void publishSims(String simsId) throws RmesException {

		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.simsGraph(simsId);
		Set<Resource> organizations = new HashSet<>();
		Set<Resource> geographies = new HashSet<>();

		try (RepositoryConnection con = repoGestion.getConnection();
			 RepositoryResult<Statement> metadataReportStatements = repoGestion.getCompleteGraph(con, graph)) {

			if (!metadataReportStatements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_ID, "Sims not found", simsId);
			}
			while (metadataReportStatements.hasNext()) {
				Statement st = metadataReportStatements.next();
				String predicate = RdfUtils.toString(st.getPredicate());
				if (isTripletForPublication(predicate)) {
					transformTripleToPublish(model, st);
					if (st.getObject() instanceof Resource resource) {
						if (isOrganization(resource)) {
							organizations.add(resource);
						} else if (isGeography(resource)) {
							geographies.add(resource);
						}
					}
				}
			}
			documentsPublication.publishAllDocumentsInSims(simsId);
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}

		repositoryPublication.publishContext(graph, model, "sims");

		// Publish referenced organizations
		for (Resource organization : organizations) {
			publicationUtils.publishResource(organization, Set.of());
		}

		// Publish referenced geographies
		for (Resource geography : geographies) {
			publicationUtils.publishResource(geography, Set.of());
		}
	}

	private boolean isTripletForPublication(String predicate) {
		return RUBRICS_NOT_FOR_PUBLICATION.stream()
			.noneMatch(predicate::endsWith);
	}

	private boolean isOrganization(Resource resource) {
		return resource.stringValue().contains(organisationsGraph);
	}

	private boolean isGeography(Resource resource) {
		return resource.stringValue().contains(documentationsGeoBaseUri);
	}

}
