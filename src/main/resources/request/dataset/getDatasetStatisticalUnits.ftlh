SELECT ?statisticalUnit
FROM <${DATASET_GRAPH}>
WHERE {
    ?uri rdf:type dcat:Dataset .
    ?uri dcterms:identifier '${ID}' .
    ?uri insee:statisticalUnit ?statisticalUnit .
    BIND(STRAFTER(STR(?uri),'/datasets/') AS ?id) .
}