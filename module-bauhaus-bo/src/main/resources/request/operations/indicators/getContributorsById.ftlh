SELECT ?contributors
WHERE { GRAPH <${OPERATIONS_GRAPH}> {
?indic a insee:StatisticalIndicator .
FILTER(STRENDS(STR(?indic),'${PRODUCT_BASE_URI}/${ID}')) .
?indic dcterms:contributor ?contributors .
}
 }
