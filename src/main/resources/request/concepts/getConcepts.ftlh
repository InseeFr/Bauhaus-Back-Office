SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel)
WHERE {
    GRAPH <${CONCEPTS_GRAPH}> {
        ?concept skos:notation ?notation .
        BIND (STR(?notation) AS ?id)
        ?concept skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        OPTIONAL{?concept skos:altLabel ?altLabelLg1 .
        FILTER (lang(?altLabelLg1) = '${LG1}')}
    }
}
GROUP BY ?id ?label
ORDER BY ?label