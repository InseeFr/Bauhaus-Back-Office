package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.port.serverside.concepts.CollectionRepository;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CollectionsUtils  {
	
	static final Logger logger = LoggerFactory.getLogger(CollectionsUtils.class);
	
	private final ConceptsPublication conceptsPublication;
	private final RepositoryGestion repositoryGestion;

	public CollectionsUtils(ConceptsPublication conceptsPublication,
							RepositoryGestion repositoryGestion
    ) {
        this.conceptsPublication = conceptsPublication;
        this.repositoryGestion = repositoryGestion;
    }


	public void collectionsValidation(String body) throws RmesException   {
		JSONArray collectionsToValidate = new JSONArray(body);
		collectionsValidation(collectionsToValidate);
	}

	
	public void collectionsValidation(JSONArray collectionsToValidate) throws  RmesException  {
		Model model = new LinkedHashModel();
		List<IRI> collectionsToValidateList = new ArrayList<>();
		for (int i = 0; i < collectionsToValidate.length(); i++) {
			IRI collectionURI = RdfUtils.collectionIRI(collectionsToValidate.getString(i).replace(" ", "").toLowerCase());
			collectionsToValidateList.add(collectionURI);
			model.add(collectionURI, INSEE.IS_VALIDATED, RdfUtils.setLiteralBoolean(true), RdfUtils.conceptGraph());
			logger.info("Validate collection : {}" , collectionURI);
		}

		repositoryGestion.objectsValidation(collectionsToValidateList, model);
		conceptsPublication.publishCollection(collectionsToValidate);
	}

}
