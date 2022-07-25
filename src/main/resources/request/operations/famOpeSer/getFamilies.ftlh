SELECT DISTINCT ?id ?label
WHERE {
    GRAPH <${OPERATIONS_GRAPH}>
    {
        ?family a insee:StatisticalOperationFamily .
        ?family skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        BIND(STRAFTER(STR(?family),'/operations/famille/') AS ?id) .
    }
}
GROUP BY ?id ?label
ORDER BY ?label