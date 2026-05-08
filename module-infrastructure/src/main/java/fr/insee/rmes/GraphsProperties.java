package fr.insee.rmes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GraphsProperties {

    private final String baseGraph;
    private final String conceptsGraphSuffix;
    private final String classifFamiliesGraphSuffix;
    private final String operationsGraphSuffix;
    private final String documentationsGraphSuffix;
    private final String msdGraphSuffix;
    private final String msdConceptsGraphSuffix;
    private final String documentationsGeoGraphSuffix;
    private final String documentsGraphSuffix;
    private final String productsGraphSuffix;
    private final String structuresGraphSuffix;
    private final String structuresComponentsGraphSuffix;
    private final String codeListsGraphSuffix;
    private final String organizationsGraphSuffix;
    private final String orgInseeGraphSuffix;
    private final String geographyGraphSuffix;

    public GraphsProperties(
            @Value("${fr.insee.rmes.bauhaus.baseGraph}") String baseGraph,
            @Value("${fr.insee.rmes.bauhaus.concepts.graph}") String conceptsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.classifications.families.graph}") String classifFamiliesGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.operations.graph}") String operationsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.documentations.graph}") String documentationsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.documentations.msd.graph}") String msdGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.documentations.concepts.graph}") String msdConceptsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.documentation.geographie.graph}") String documentationsGeoGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.documents.graph}") String documentsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.products.graph}") String productsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.structures.graph}") String structuresGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.structures.components.graph}") String structuresComponentsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.codelists.graph}") String codeListsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.organisations.graph}") String organizationsGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.insee.graph}") String orgInseeGraphSuffix,
            @Value("${fr.insee.rmes.bauhaus.geographie.graph}") String geographyGraphSuffix) {
        this.baseGraph = baseGraph;
        this.conceptsGraphSuffix = conceptsGraphSuffix;
        this.classifFamiliesGraphSuffix = classifFamiliesGraphSuffix;
        this.operationsGraphSuffix = operationsGraphSuffix;
        this.documentationsGraphSuffix = documentationsGraphSuffix;
        this.msdGraphSuffix = msdGraphSuffix;
        this.msdConceptsGraphSuffix = msdConceptsGraphSuffix;
        this.documentationsGeoGraphSuffix = documentationsGeoGraphSuffix;
        this.documentsGraphSuffix = documentsGraphSuffix;
        this.productsGraphSuffix = productsGraphSuffix;
        this.structuresGraphSuffix = structuresGraphSuffix;
        this.structuresComponentsGraphSuffix = structuresComponentsGraphSuffix;
        this.codeListsGraphSuffix = codeListsGraphSuffix;
        this.organizationsGraphSuffix = organizationsGraphSuffix;
        this.orgInseeGraphSuffix = orgInseeGraphSuffix;
        this.geographyGraphSuffix = geographyGraphSuffix;
    }

    public String baseGraph() {
        return baseGraph;
    }

    public String conceptsGraph() {
        return baseGraph + conceptsGraphSuffix;
    }

    public String classifFamiliesGraph() {
        return baseGraph + classifFamiliesGraphSuffix;
    }

    public String operationsGraph() {
        return baseGraph + operationsGraphSuffix;
    }

    public String documentationsGraph() {
        return baseGraph + documentationsGraphSuffix;
    }

    public String msdGraph() {
        return baseGraph + msdGraphSuffix;
    }

    public String msdConceptsGraph() {
        return baseGraph + msdConceptsGraphSuffix;
    }

    public String documentationsGeoGraph() {
        return baseGraph + documentationsGeoGraphSuffix;
    }

    public String documentsGraph() {
        return baseGraph + documentsGraphSuffix;
    }

    public String productsGraph() {
        return baseGraph + productsGraphSuffix;
    }

    public String structuresGraph() {
        return baseGraph + structuresGraphSuffix;
    }

    public String structuresComponentsGraph() {
        return baseGraph + structuresComponentsGraphSuffix;
    }

    public String codeListGraph() {
        return baseGraph + codeListsGraphSuffix;
    }

    public String organizationsGraph() {
        return baseGraph + organizationsGraphSuffix;
    }

    public String orgInseeGraph() {
        return baseGraph + orgInseeGraphSuffix;
    }

    public String geographyGraph() {
        return baseGraph + geographyGraphSuffix;
    }
}
