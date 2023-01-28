ASK
FROM <${OPERATIONS_GRAPH}>
WHERE {
    ?uri a ${TYPE} .
    {?uri skos:prefLabel "${LABEL}"@${LANG} .} UNION {?uri rdfs:label "${LABEL}"@${LANG} .}
    FILTER(!STRENDS(STR(?uri),'${URI_PREFIX}${ID}')) .
}