package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.Config;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.*;

public abstract class RdfService {

	protected final RepositoryGestion repoGestion;

	protected final IdGenerator idGenerator;

	protected final RepositoryPublication repositoryPublication;

	protected final Config config;

	protected final PublicationUtils publicationUtils;

	protected RdfService(RepositoryGestion repoGestion, IdGenerator idGenerator,
						 RepositoryPublication repositoryPublication, Config config,
						 PublicationUtils publicationUtils) {
		this.repoGestion = repoGestion;
		this.idGenerator = idGenerator;
		this.repositoryPublication = repositoryPublication;
		this.config = config;
		this.publicationUtils = publicationUtils;
	}

	public void transformTripleToPublish(Model model, Statement st) {
		Resource subject = publicationUtils.tranformBaseURIToPublish(st.getSubject());
		IRI predicateIRI = RdfUtils
				.createIRI(publicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
		Value object = st.getObject();
		if (st.getObject() instanceof Resource resource) {
            object = publicationUtils.tranformBaseURIToPublish(resource);
		}

		model.add(subject, predicateIRI, object, st.getContext());
	}
}
