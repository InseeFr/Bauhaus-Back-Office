SELECT DISTINCT ?id ?label ?creator
WHERE {
    ?collection rdf:type skos:Collection .
    BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) .
    ?collection dcterms:title ?label .
    ?collection dc:creator ?creator .
    ?collection insee:isValidated 'false'^^xsd:boolean .
    FILTER (lang(?label) = '${LG1}')
}
ORDER BY ?label
