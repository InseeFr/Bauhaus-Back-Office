SELECT DISTINCT ?id ?prefLabelLg1 ?prefLabelLg2 (group_concat(?abstractL1;separator=' || ') as ?abstractLg1) ?abstractLg2
WHERE {
    GRAPH <${OPERATIONS_GRAPH}> {
        ?family a insee:StatisticalOperationFamily .
        ?family skos:prefLabel ?prefLabelLg1 .
        FILTER (lang(?prefLabelLg1) = '${LG1}')

        OPTIONAL {?family skos:prefLabel ?prefLabelLg2 .
        FILTER (lang(?prefLabelLg2) = '${LG2}') } .

        OPTIONAL {?family dcterms:abstract ?abstractL1 .
        FILTER (lang(?abstractL1) = '${LG1}') } .

        OPTIONAL {?family dcterms:abstract ?abstractLg2 .
        FILTER (lang(?abstractLg2) = '${LG2}') } .

        BIND(STRAFTER(STR(?family),'/operations/famille/') AS ?id) .
    }
}
GROUP BY ?id ?prefLabelLg1 ?prefLabelLg2 ?abstractL1 ?abstractLg2
ORDER BY ?prefLabelLg1