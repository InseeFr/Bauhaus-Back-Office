SELECT ?id ?label
FROM <${DATASET_GRAPH}>
WHERE {
    ?uri rdf:type dcat:Dataset .
    ?uri dcterms:title ?label
    FILTER (lang(?label) = '${LG1}') .
    BIND(STRAFTER(STR(?uri),'/catalogues/jeuDeDonnees/') AS ?id) .
}