SELECT ?item
WHERE {
    ?item skos:inScheme ?classification .
    FILTER(REGEX(STR(?classification),'/codes/${CLASSIFICATION_ID}/'))
    FILTER(STRENDS(STR(?item),'${CLASSIFICATION_ITEM_ID}'))
}