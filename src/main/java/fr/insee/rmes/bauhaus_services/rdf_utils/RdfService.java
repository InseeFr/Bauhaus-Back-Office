package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

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
	protected StampsRestrictionsService stampsRestrictionsService;

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

	public void getMultipleTripletsForObject(JSONObject object, String objectKey, String query, String queryKey) throws RmesException {
		JSONArray array = this.repoGestion.getResponseAsArray(query);
		List<String> results = new ArrayList<>();
		if(array == null){
			return;
		}
		array.iterator().forEachRemaining(r -> results.add(((JSONObject) r).getString(queryKey)));
		object.put(objectKey, results);
	}
}
