SELECT ?id ?created ?modified
WHERE { 
    GRAPH <${CONCEPTS_GRAPH}> {
        ?concept skos:prefLabel ?prefLabel .
        FILTER(REGEX(STR(?concept),'/concepts/definition/')) .
        BIND(STRAFTER(STR(?concept),'/definition/') AS ?id) .
        
        ?concept dcterms:created ?created .
        OPTIONAL {?concept dcterms:modified ?modified} .
    }
}
ORDER BY ?id