SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel)
WHERE {
    GRAPH <${OPERATIONS_GRAPH}> {
        ?operation a insee:StatisticalOperation .
        ?operation skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        BIND(STRAFTER(STR(?operation),'/operations/operation/') AS ?id) .

        OPTIONAL{
            ?operation skos:altLabel ?altLabelLg1 .
            FILTER (lang(?altLabelLg1) = '${LG2}')
        }
    }
}
GROUP BY ?id ?label
ORDER BY ?label