package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.DocumentationsProperties;
import fr.insee.rmes.GraphsProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RdfUtilsInitializer {

    private final GraphsProperties graphs;
    private final BauhausUriProperties uris;
    private final DocumentationsProperties documentations;

    public RdfUtilsInitializer(GraphsProperties graphs, BauhausUriProperties uris, DocumentationsProperties documentations) {
        this.graphs = graphs;
        this.uris = uris;
        this.documentations = documentations;
    }

    @PostConstruct
    public void initRdfUtils() {
        RdfUtils.setGraphs(this.graphs);
        RdfUtils.setUris(this.uris);
        RdfUtils.setDocumentations(this.documentations);
    }
}
