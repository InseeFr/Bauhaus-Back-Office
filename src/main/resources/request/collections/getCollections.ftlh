SELECT DISTINCT ?id ?label
WHERE {
    ?collection rdf:type skos:Collection .
    BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) .
    ?collection dcterms:title ?label .
    FILTER (lang(?label) = '${LG1}')
}
ORDER BY ?label