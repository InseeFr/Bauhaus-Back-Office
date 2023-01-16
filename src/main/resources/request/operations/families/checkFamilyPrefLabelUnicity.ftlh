ASK
FROM <${OPERATIONS_GRAPH}>
WHERE {
    ?family a insee:StatisticalOperationFamily .
    ?family skos:prefLabel "${LABEL}"@${LANG} .
    FILTER(!STRENDS(STR(?family),'/operations/famille/${ID}')) .
}