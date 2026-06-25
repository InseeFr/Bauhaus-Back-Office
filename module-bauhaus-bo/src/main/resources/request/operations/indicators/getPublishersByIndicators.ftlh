SELECT ?indicatorId ?publishers
WHERE { GRAPH <${OPERATIONS_GRAPH}> {
?indic a insee:StatisticalIndicator .
BIND(STRAFTER(STR(?indic),'/${PRODUCT_BASE_URI}/') AS ?indicatorId) .
?indic dcterms:publisher ?publishers .
} }
ORDER BY ?indicatorId
