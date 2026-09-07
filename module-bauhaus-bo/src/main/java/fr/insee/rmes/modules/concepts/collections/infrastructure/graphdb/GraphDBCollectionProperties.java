package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.regex.Pattern;

@ConfigurationProperties("fr.insee.rmes.bauhaus.collections")
public class GraphDBCollectionProperties {

    /**
     * `collections.baseURI` est composée en concaténant la base URI de gestion et
     * `/concepts/definitions`. Quand la base URI de gestion se termine par `/`, le
     * séparateur est doublé et l'IRI produite désigne une ressource distincte de la
     * collection historique : la même collection existe alors deux fois, et sa
     * publication échoue en « Collections already published ».
     */
    private static final Pattern DUPLICATED_PATH_SEPARATORS = Pattern.compile("(?<!:)//+");

    private final String graph;
    private final String baseURI;
    private static ValueFactory factory =  SimpleValueFactory.getInstance();

    public GraphDBCollectionProperties(String graph, String baseURI) {
        this.graph = graph;
        this.baseURI = baseURI;
    }

    public IRI getResourceIRI(String id){
        var iri = "%s/%s".formatted(baseURI, id);
        return factory.createIRI(DUPLICATED_PATH_SEPARATORS.matcher(iri).replaceAll("/"));
    }

    IRI getResourceGraph(){
        return factory.createIRI(graph);
    }
}
