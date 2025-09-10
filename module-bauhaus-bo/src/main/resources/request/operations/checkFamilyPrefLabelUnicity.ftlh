ASK
FROM <${OPERATIONS_GRAPH}>
WHERE {
    ?uri a ${TYPE} .
    {?uri skos:prefLabel "${LABEL?no_esc}"@${LANG} .} UNION {?uri rdfs:label "${LABEL?no_esc}"@${LANG} .}
    FILTER(!STRENDS(STR(?uri),'${URI_PREFIX}${ID}')) .
}