package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fr.insee.rmes.bauhaus.collections")
public class GraphDBCollectionProperties {

    private final String graph;
    private final String baseURI;
    private static ValueFactory factory =  SimpleValueFactory.getInstance();

    public GraphDBCollectionProperties(String graph, String baseURI) {
        this.graph = graph;
        this.baseURI = baseURI;
    }

    IRI getResourceIRI(String id){
        var iri = "%s/%s".formatted(baseURI, id);
        return factory.createIRI(iri);
    }

    IRI getResourceGraph(){
        return factory.createIRI(graph);
    }
}
