package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb.GraphDBCollectionProperties;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.json.JSONUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class LegacyCollectionsRepository  {
	
	static final Logger logger = LoggerFactory.getLogger(LegacyCollectionsRepository.class);
	
	private final ConceptsPublication conceptsPublication;
	private final RepositoryGestion repositoryGestion;
	private final GraphDBCollectionProperties collectionProperties;

	public LegacyCollectionsRepository(ConceptsPublication conceptsPublication,
									   RepositoryGestion repositoryGestion,
									   GraphDBCollectionProperties collectionProperties
    ) {
        this.conceptsPublication = conceptsPublication;
        this.repositoryGestion = repositoryGestion;
        this.collectionProperties = collectionProperties;
    }


	public void collectionsValidation(String body) throws RmesException   {
		JSONArray collectionsToValidate = new JSONArray(body);
		collectionsValidation(collectionsToValidate);
	}

	
	public void collectionsValidation(JSONArray collectionsToValidate) throws  RmesException  {
		Model model = new LinkedHashModel();
		List<IRI> collectionsToValidateList = new ArrayList<>();
		JSONUtils.jsonArrayToList(collectionsToValidate).forEach(collectionId -> {
			IRI collectionURI = collectionProperties.getResourceIRI(collectionId.replace(" ", ""));
			collectionsToValidateList.add(collectionURI);
			model.add(collectionURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.conceptGraph());
			logger.info("Validate collection : {}" , collectionURI);
		});

		repositoryGestion.objectsValidation(collectionsToValidateList, model);
		conceptsPublication.publishCollection(collectionsToValidate);
	}

}
