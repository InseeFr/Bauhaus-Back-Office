ASK
FROM <${OPERATIONS_GRAPH}>
WHERE {
    ?family a insee:${TYPE} .
    ?family skos:prefLabel "${LABEL}"@${LANG} .
    FILTER(!STRENDS(STR(?family),'${URI_PREFIX}${ID}')) .
}