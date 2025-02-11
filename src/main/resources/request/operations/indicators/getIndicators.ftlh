SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel)
WHERE {
    GRAPH <${PRODUCTS_GRAPH}> {
        ?indic a insee:StatisticalIndicator .
        ?indic skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')

        BIND(STRAFTER(STR(?indic),'/${PRODUCT_BASE_URI}/') AS ?id) .

        OPTIONAL{
            ?indic skos:altLabel ?altLabelLg1 .
            FILTER (lang(?altLabelLg1) = '${LG1}')
        }
    }
}
GROUP BY ?id ?label ?altLabelLg1
ORDER BY ?label