package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb.GraphDBCollectionProperties;
import fr.insee.rmes.modules.shared_kernel.infrastructure.publication.ObjectPublished;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@Repository
public class LegacyCollectionsRepository {

    static final Logger logger = LoggerFactory.getLogger(LegacyCollectionsRepository.class);

    private final ConceptsPublication conceptsPublication;
    private final GraphDBCollectionProperties collectionProperties;
    private final ApplicationEventPublisher events;

    public LegacyCollectionsRepository(
            ConceptsPublication conceptsPublication,
            GraphDBCollectionProperties collectionProperties,
            ApplicationEventPublisher events) {
        this.conceptsPublication = conceptsPublication;
        this.collectionProperties = collectionProperties;
        this.events = events;
    }

    /**
     * Publie les collections une à une : chacune n'est annoncée publiée, donc passée « Validated » par
     * l'écouteur, qu'une fois sa copie publique faite. Un échec laisse la collection et les suivantes
     * dans leur statut, re-validables.
     */
    public void collectionsValidation(JSONArray collectionsToValidate) throws RmesException {
        for (String collectionId : JSONUtils.jsonArrayToList(collectionsToValidate)) {
            IRI collectionURI = collectionProperties.getResourceIRI(collectionId);
            logger.info("Validate collection : {}", collectionURI);
            conceptsPublication.publishCollection(new JSONArray().put(collectionId), RdfUtils.conceptGraph());
            events.publishEvent(new ObjectPublished(collectionURI, RdfUtils.conceptGraph()));
        }
    }
}
