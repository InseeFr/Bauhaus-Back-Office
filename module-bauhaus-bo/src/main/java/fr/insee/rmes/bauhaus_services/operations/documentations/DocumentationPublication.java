package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.ontologies.DCMITYPE;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.modules.operations.msd.DocumentationConfiguration;
import fr.insee.rmes.modules.organisations.OrganisationsProperties;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class DocumentationPublication {

	private final Logger logger = LoggerFactory.getLogger(DocumentationPublication.class);

	private final RepositoryGestion repoGestion;
	private final RepositoryPublication repositoryPublication;
	private final PublicationUtils publicationUtils;
	private final DocumentsPublication documentsPublication;
	private final DocumentationConfiguration documentationConfiguration;
	private final OrganisationsProperties organisationsProperties;

	private static final Set<String> RUBRICS_NOT_FOR_PUBLICATION = Set.of("S.1.3","S.1.4","S.1.5","S.1.6","S.1.7","S.1.8","validationState");
	private static final String SIMS_CONTEXT_TYPE = "sims";

	public DocumentationPublication(
			RepositoryGestion repoGestion,
			RepositoryPublication repositoryPublication,
			PublicationUtils publicationUtils,
			DocumentsPublication documentsPublication,
			DocumentationConfiguration documentationConfiguration,
			OrganisationsProperties organisationsProperties) {
		this.repoGestion = repoGestion;
		this.repositoryPublication = repositoryPublication;
		this.publicationUtils = publicationUtils;
		this.documentsPublication = documentsPublication;
		this.documentationConfiguration = documentationConfiguration;
		this.organisationsProperties = organisationsProperties;
	}

	/**
	 * Publishes a SIMS (Statistical Information Management System) metadata report.
	 * This method transforms and publishes the SIMS data along with all referenced
	 * organizations and geographies. It also converts Markdown content to HTML for
	 * rich text rubrics.
	 *
	 * @param simsId The unique identifier of the SIMS to publish
	 * @throws RmesNotFoundException if the SIMS with the given ID does not exist
	 * @throws RmesException if the publication process fails
	 */
	public void publishSims(String simsId) throws RmesException {
		if (simsId == null || simsId.isBlank()) {
			throw new RmesException(HttpStatus.SC_BAD_REQUEST,
				"simsId cannot be null or empty", "Invalid SIMS ID");
		}

		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.simsGraph(simsId);
		Set<Resource> organizations = new HashSet<>();
		Set<Resource> geographies = new HashSet<>();
		Set<Resource> richTextResources = new HashSet<>();
		Map<Resource, String> markdownValues = new HashMap<>();

		try (RepositoryConnection con = repoGestion.getConnection();
			 RepositoryResult<Statement> metadataReportStatements = repoGestion.getCompleteGraph(con, graph)) {

			if (!metadataReportStatements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_ID, "Sims not found", simsId);
			}
			while (metadataReportStatements.hasNext()) {
				processStatement(metadataReportStatements.next(), model,
					richTextResources, markdownValues, organizations, geographies);
			}

			// Add HTML version for RICHTEXT rubrics (single pass)
			addHtmlVersionForRichText(model, richTextResources, markdownValues);

			documentsPublication.publishAllDocumentsInSims(simsId);
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}

		repositoryPublication.publishContext(graph, model, SIMS_CONTEXT_TYPE);

		publishReferencedResources(organizations, geographies);
	}

	private boolean isTripletForPublication(String predicate) {
		return RUBRICS_NOT_FOR_PUBLICATION.stream()
			.noneMatch(predicate::endsWith);
	}

	private boolean isOrganization(Resource resource) {
		if (resource == null || organisationsProperties.graph() == null) {
			return false;
		}
		String resourceValue = resource.stringValue();
		return resourceValue != null && resourceValue.contains(organisationsProperties.graph());
	}

	private boolean isGeography(Resource resource) {
		if (resource == null || documentationConfiguration.geographie() == null) {
			return false;
		}
		String resourceValue = resource.stringValue();
		return resourceValue != null && resourceValue.contains(documentationConfiguration.geographie().baseUri());
	}

	private void transformTripleToPublish(Model model, Statement st) {
		Resource subject = publicationUtils.tranformBaseURIToPublish(st.getSubject());
		IRI predicateIRI = RdfUtils
				.createIRI(publicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
		org.eclipse.rdf4j.model.Value object = st.getObject();
		if (st.getObject() instanceof Resource resource) {
			object = publicationUtils.tranformBaseURIToPublish(resource);
		}

		model.add(subject, predicateIRI, object, st.getContext());
	}

	private void processStatement(Statement st, Model model,
	                              Set<Resource> richTextResources,
	                              Map<Resource, String> markdownValues,
	                              Set<Resource> organizations,
	                              Set<Resource> geographies) {
		String predicate = RdfUtils.toString(st.getPredicate());
		if (!isTripletForPublication(predicate)) {
			return;
		}

		transformTripleToPublish(model, st);
		trackTextResources(st, richTextResources, markdownValues);
		trackReferencedResources(st, organizations, geographies);
	}

	private void trackTextResources(Statement st, Set<Resource> richTextResources,
	                                Map<Resource, String> markdownValues) {
		// Track TEXT resources - order of statements is not guaranteed
		if (RDF.TYPE.equals(st.getPredicate()) && DCMITYPE.TEXT.equals(st.getObject())) {
			richTextResources.add(st.getSubject());
		}

		// Store ALL rdf:value - we'll filter by TEXT type later
		// (rdf:value may arrive before rdf:type in the result set)
		if (RDF.VALUE.equals(st.getPredicate())) {
			markdownValues.put(st.getSubject(), st.getObject().stringValue());
		}
	}

	private void trackReferencedResources(Statement st, Set<Resource> organizations,
	                                      Set<Resource> geographies) {
		if (st.getObject() instanceof Resource resource) {
			if (isOrganization(resource)) {
				organizations.add(resource);
			} else if (isGeography(resource)) {
				geographies.add(resource);
			}
		}
	}

	private void publishReferencedResources(Set<Resource> organizations,
	                                        Set<Resource> geographies) throws RmesException {
		// Combine both sets and publish in one loop
		Set<Resource> allResources = new HashSet<>();
		allResources.addAll(organizations);
		allResources.addAll(geographies);

		for (Resource resource : allResources) {
			publicationUtils.publishResource(resource, Set.of());
		}
	}

	/**
	 * For each RICHTEXT rubric (identified by dcmitype:Text type),
	 * convert the Markdown content to HTML and add it with insee:html predicate.
	 *
	 * @param model Target model for publication
	 * @param richTextResources Set of TEXT type resources
	 * @param markdownValues Map of TEXT resources to their Markdown content
	 */
	private void addHtmlVersionForRichText(Model model, Set<Resource> richTextResources, Map<Resource, String> markdownValues) {
		for (Resource textResource : richTextResources) {
			String markdownContent = markdownValues.get(textResource);

			if (markdownContent != null) {
				try {
					// Convert Markdown to HTML
					String htmlContent = XhtmlToMarkdownUtils.markdownToXhtml(markdownContent);

					// Transform URIs for publication
					Resource publishedSubject = publicationUtils.tranformBaseURIToPublish(textResource);

					// Add the HTML version with insee:html predicate
					model.add(publishedSubject, INSEE.HTML,
							RdfUtils.setLiteralString(htmlContent),
							publicationUtils.tranformBaseURIToPublish(RdfUtils.simsGraph(null)));
				} catch (Exception e) {
					// Log error but continue processing other resources
					logger.error("Error processing TEXT resource {}: {}", textResource, e.getMessage());
				}
			}
		}
	}

}
