SELECT DISTINCT ?id ?label ?altLabel
WHERE {
    GRAPH <${CONCEPTS_GRAPH}> {
        ?concept skos:notation ?notation .
        BIND (STR(?notation) AS ?id)
        ?concept skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        OPTIONAL{?concept skos:altLabel ?altLabel .
        FILTER (lang(?altLabel) = '${LG1}')}
    }
}
ORDER BY ?label