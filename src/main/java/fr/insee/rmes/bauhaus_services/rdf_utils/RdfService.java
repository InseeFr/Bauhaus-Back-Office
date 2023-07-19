package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.concepts.collections.ConceptUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import org.eclipse.rdf4j.model.*;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RdfService {

	@Autowired
	protected RepositoryGestion repoGestion;

	@Autowired
	protected RepositoryPublication repositoryPublication;
	
	@Autowired
	protected Config config;
	
	@Autowired
	protected StampsRestrictionsService stampsRestrictionsService;

	@Autowired
	protected ConceptUtils conceptUtils;

	public void transformTripleToPublish(Model model, Statement st) {
		Resource subject = PublicationUtils.tranformBaseURIToPublish(st.getSubject());
		IRI predicateIRI = RdfUtils
				.createIRI(PublicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
		Value object = st.getObject();
		if (st.getObject() instanceof Resource) {
			object = PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject());
		}

		model.add(subject, predicateIRI, object, st.getContext());
	}

	
}
