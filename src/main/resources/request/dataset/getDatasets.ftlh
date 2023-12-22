SELECT ?id ?label
FROM <${DATASET_GRAPH}>
WHERE {
    ?uri rdf:type dcat:Dataset .
    ?uri dcterms:identifier ?id .
    ?uri dcterms:title ?label
    FILTER (lang(?label) = '${LG1}') .
}