SELECT DISTINCT ?id ?label ?altLabel
WHERE {
    GRAPH <${PRODUCTS_GRAPH}> {
        ?indic a insee:StatisticalIndicator .
        ?indic skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')

        BIND(STRAFTER(STR(?indic),'/${PRODUCT_BASE_URI}/') AS ?id) .

        OPTIONAL{
            ?indic skos:altLabel ?altLabel .
            FILTER (lang(?altLabel) = '${LG1}')
        }
    }
}
ORDER BY ?label