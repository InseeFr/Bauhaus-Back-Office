SELECT ?contributors
WHERE { GRAPH ${OPERATIONS_GRAPH} {
?indic a insee:StatisticalIndicator .
FILTER(STRENDS(STR(?indic),${INDICATOR_URI_SUFFIX})) .
?indic dcterms:contributor ?contributors .
}
 }
