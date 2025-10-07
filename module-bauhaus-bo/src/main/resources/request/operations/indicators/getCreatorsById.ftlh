SELECT ?creators
WHERE { GRAPH <${OPERATIONS_GRAPH}> {
?indic a insee:StatisticalIndicator .
FILTER(STRENDS(STR(?indic),'/${PRODUCT_BASE_URI}/${ID}')) .
?indic dc:creator ?creators .
} }
