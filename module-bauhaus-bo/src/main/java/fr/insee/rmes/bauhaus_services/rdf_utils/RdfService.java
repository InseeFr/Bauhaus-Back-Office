package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.*;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RdfService {


	@Autowired
	protected RepositoryGestion repoGestion;

	@Autowired
	protected IdGenerator idGenerator;

	@Autowired
	protected RepositoryPublication repositoryPublication;
	
	@Autowired
	protected Config config;


	@Autowired
	protected PublicationUtils publicationUtils;
	
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
