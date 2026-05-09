package fr.insee.rmes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = GraphsProperties.class)
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.baseGraph=http://test.graph/",
        "fr.insee.rmes.bauhaus.concepts.graph=concepts",
        "fr.insee.rmes.bauhaus.classifications.families.graph=classif/families",
        "fr.insee.rmes.bauhaus.operations.graph=operations",
        "fr.insee.rmes.bauhaus.documentations.graph=docs",
        "fr.insee.rmes.bauhaus.documentations.msd.graph=msd",
        "fr.insee.rmes.bauhaus.documentations.concepts.graph=msd-concepts",
        "fr.insee.rmes.bauhaus.documentation.geographie.graph=geo-docs",
        "fr.insee.rmes.bauhaus.documents.graph=documents",
        "fr.insee.rmes.bauhaus.products.graph=products",
        "fr.insee.rmes.bauhaus.structures.graph=structures",
        "fr.insee.rmes.bauhaus.structures.components.graph=components",
        "fr.insee.rmes.bauhaus.codelists.graph=codelists",
        "fr.insee.rmes.bauhaus.organisations.graph=orgs",
        "fr.insee.rmes.bauhaus.insee.graph=orgs/insee",
        "fr.insee.rmes.bauhaus.geographie.graph=geo"
})
class GraphsPropertiesTest {

    @Autowired
    private GraphsProperties graphs;

    @Test
    void shouldReturnBaseGraph() {
        assertEquals("http://test.graph/", graphs.baseGraph());
    }

    @Test
    void shouldConcatBaseGraphWithSuffixesForEachDomain() {
        assertEquals("http://test.graph/concepts", graphs.conceptsGraph());
        assertEquals("http://test.graph/classif/families", graphs.classifFamiliesGraph());
        assertEquals("http://test.graph/operations", graphs.operationsGraph());
        assertEquals("http://test.graph/docs", graphs.documentationsGraph());
        assertEquals("http://test.graph/msd", graphs.msdGraph());
        assertEquals("http://test.graph/msd-concepts", graphs.msdConceptsGraph());
        assertEquals("http://test.graph/geo-docs", graphs.documentationsGeoGraph());
        assertEquals("http://test.graph/documents", graphs.documentsGraph());
        assertEquals("http://test.graph/products", graphs.productsGraph());
        assertEquals("http://test.graph/structures", graphs.structuresGraph());
        assertEquals("http://test.graph/components", graphs.structuresComponentsGraph());
        assertEquals("http://test.graph/codelists", graphs.codeListGraph());
        assertEquals("http://test.graph/orgs", graphs.organizationsGraph());
        assertEquals("http://test.graph/orgs/insee", graphs.orgInseeGraph());
        assertEquals("http://test.graph/geo", graphs.geographyGraph());
    }
}
