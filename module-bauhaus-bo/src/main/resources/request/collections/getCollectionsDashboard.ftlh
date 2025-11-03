SELECT ?id ?label ?created ?modified ?isValidated ?creator
(count(?member) AS ?nbMembers)
WHERE {
    ?collection rdf:type skos:Collection .
    BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) .
    ?collection dcterms:title ?label .
    ?collection dcterms:created ?created .
    OPTIONAL {?collection dcterms:modified ?modified} .
    ?collection insee:isValidated ?isValidated
    OPTIONAL {?collection dc:creator ?creator} .
    FILTER (lang(?label) = '${LG1}')
    ?collection skos:member ?member .
}
GROUP BY ?id ?label ?created ?modified ?isValidated ?creator
ORDER BY ?label
