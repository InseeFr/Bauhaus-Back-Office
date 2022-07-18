package fr.insee.rmes.bauhaus_services.rdf_utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;

public abstract class RdfService {

	@Autowired
	protected RepositoryGestion repoGestion;
	
	@Autowired
	protected Config config;
	
	@Autowired
	protected StampsRestrictionsService stampsRestrictionsService;
	
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
