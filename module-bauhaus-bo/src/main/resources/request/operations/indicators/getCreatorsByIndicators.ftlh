SELECT ?indicatorId ?creators
WHERE { GRAPH <${OPERATIONS_GRAPH}> {
?indic a insee:StatisticalIndicator .
BIND(STRAFTER(STR(?indic),'/${PRODUCT_BASE_URI}/') AS ?indicatorId) .
?indic dc:creator ?creators .
} }
ORDER BY ?indicatorId
