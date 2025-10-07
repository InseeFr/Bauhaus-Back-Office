SELECT ?spacialResolution
FROM <${DATASET_GRAPH}>
WHERE {
    ?uri rdf:type dcat:Dataset .
    ?uri dcterms:identifier '${ID}' .
    ?uri insee:spatialResolution ?spacialResolution .

    BIND(STRAFTER(STR(?uri),'/datasets/') AS ?id) .
}