package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.graphdb.ObjectType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.stereotype.Component;

/**
 * Successeur injectable de la partie « à état » de {@link RdfUtils} : les IRI et les graphes qui
 * se déduisent de la configuration.
 *
 * <p>Là où {@code RdfUtils} recevait ses propriétés par des setters statiques appelés au
 * démarrage, ce composant les reçoit par son constructeur : le conteneur Spring garantit qu'il
 * est complet avant d'être utilisé, et un test se contente de lui passer les propriétés qu'il
 * veut.
 */
@Component
public class BauhausIriFactory {

    private final GraphsProperties graphs;
    private final BauhausUriBuilder uriBuilder;

    public BauhausIriFactory(GraphsProperties graphs, BauhausUriBuilder uriBuilder) {
        this.graphs = graphs;
        this.uriBuilder = uriBuilder;
    }

    public Resource structureComponentGraph() {
        return RdfTriples.iri(graphs.structuresComponentsGraph());
    }

    public IRI structureComponentAttribute(String id) {
        return objectIri(ObjectType.ATTRIBUTE_PROPERTY, id);
    }

    public IRI structureComponentDimension(String id) {
        return objectIri(ObjectType.DIMENSION_PROPERTY, id);
    }

    public IRI structureComponentMeasure(String id) {
        return objectIri(ObjectType.MEASURE_PROPERTY, id);
    }

    /**
     * URI de base des concepts, à laquelle un identifiant de concept est concaténé. Renvoyée sous
     * forme de chaîne, et non d'IRI, parce que ce préfixe n'est pas lui-même une ressource.
     */
    public String conceptBaseUri() {
        return uriBuilder.getBaseUriGestion(ObjectType.CONCEPT);
    }

    private IRI objectIri(ObjectType objectType, String id) {
        if (isAbsoluteUri(id)) {
            return RdfTriples.iri(id);
        }
        return RdfTriples.iri(uriBuilder.getBaseUriGestion(objectType) + "/" + id);
    }

    private static boolean isAbsoluteUri(String value) {
        return value != null && (value.startsWith("http://") || value.startsWith("https://"));
    }
}
