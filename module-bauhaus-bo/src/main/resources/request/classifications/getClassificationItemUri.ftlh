SELECT ?item
WHERE {
    ?item skos:inScheme ?classification .
    FILTER(REGEX(STR(?classification),${CLASSIFICATION_URI_PATTERN}))
    FILTER(STRENDS(STR(?item),${CLASSIFICATION_ITEM_ID}))
}